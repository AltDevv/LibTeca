package com.libteca.service.reserva;

import com.libteca.dto.reserva.*;
import com.libteca.dto.emprestimo.*;
import com.libteca.entity.*;
import com.libteca.enums.*;
import com.libteca.handler.reserva.exception.*;
import com.libteca.handler.livro.exception.LivroIndisponivelException;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.ReservaMapper;
import com.libteca.repository.*;
import com.libteca.service.*;
import com.libteca.service.emprestimo.EmprestimoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaMapper reservaMapper;
    private final ExemplarService exemplarService;
    private final FuncionarioService funcionarioService;
    private final ReservaLancamentoService reservaLancamentoService;
    private final EmprestimoService emprestimoService;
    private final LivroRepository livros;
    private final ExemplarRepository exemplares;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
                          ReservaMapper reservaMapper, ExemplarService exemplarService,
                          FuncionarioService funcionarioService, ReservaLancamentoService reservaLancamentoService,
                          EmprestimoService emprestimoService, LivroRepository livros, ExemplarRepository exemplares) {
        this.livros = livros; this.exemplares = exemplares;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaMapper = reservaMapper;
        this.exemplarService = exemplarService;
        this.funcionarioService = funcionarioService;
        this.reservaLancamentoService = reservaLancamentoService;
        this.emprestimoService = emprestimoService;
    }

    public Page<ReservaResponse> listarTodos(Pageable pageable) {
        return reservaRepository.findAll(pageable).map(reservaMapper::toResponse);
    }

    public ReservaResponse mostrarReserva(Long id) {
        return reservaMapper.toResponse(buscarReserva(id));
    }

    private Reserva buscarReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException("Reserva não encontrada"));
    }

    public ReservaResponse adicionarReserva(ReservaRequest request) {
        // Serializa a verificação do limite entre todos os exemplares do mesmo livro.
        Long livroId = exemplares.buscarLivroId(request.exemplarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exemplar não encontrado"));
        livros.bloquearReservas(livroId);
        Exemplar exemplar = exemplarService.bloquear(request.exemplarId());
        exemplarService.recarregar(exemplar);
        Livro livro = exemplar.getLivro();
        if (!livro.getId().equals(livroId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O exemplar foi alterado. Atualize a página");
        exemplarService.recarregar(livro);
        if (livro.getLimiteReservas() == 0)
            throw new LivroIndisponivelException("Este livro não permite reservas");
        var copias = exemplares.buscarPorLivroParaAtualizar(livro.getId());
        var fila = reservaRepository.findByExemplarLivroIdAndStatusIn(livro.getId(), ReservaLancamentoService.EM_ANDAMENTO);
        if (fila.size() >= livro.getLimiteReservas())
            throw new LivroIndisponivelException("O limite de reservas deste livro foi atingido");
        if (!livro.getDataLancamento().isAfter(java.time.LocalDate.now()) && copias.stream()
                .anyMatch(e -> e.getTipo() == exemplar.getTipo() && e.getStatus() == StatusExemplar.DISPONIVEL))
            throw new LivroIndisponivelException("Há exemplar disponível neste formato. Solicite o empréstimo");
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        Funcionario funcionario = funcionarioService.buscarAtivo(request.funcionarioId());
        if (exemplar.getStatus() == StatusExemplar.INDISPONIVEL) {
            throw new LivroIndisponivelException("Este exemplar está indisponível");
        }
        if (fila.stream().anyMatch(r -> r.getUsuario().getId().equals(usuario.getId()))) {
            throw new JaPossuiReservaException("Você já possui uma reserva em andamento para este livro");
        }
        Reserva reserva = reservaMapper.toEntity(exemplar, usuario, funcionario);
        reservaRepository.saveAndFlush(reserva);
        reservaLancamentoService.atualizarDisponibilidade(exemplar);
        return reservaMapper.toResponse(reserva);
    }

    public ReservaResponse cancelarReserva(Long id) {
        Exemplar exemplar = exemplarService.bloquear(buscarReserva(id).getExemplar().getId());
        Reserva reserva = reservaRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException("Reserva não encontrada"));
        exemplarService.recarregar(reserva);
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ReservaFoiCanceladaException("Esta reserva já está cancelada");
        }
        if (!ReservaLancamentoService.EM_ANDAMENTO.contains(reserva.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta reserva já foi encerrada");
        }
        reserva.setStatus(StatusReserva.CANCELADA);
        reservaRepository.flush();
        reservaLancamentoService.atualizarDisponibilidade(exemplar);
        return reservaMapper.toResponse(reserva);
    }

    public EmprestimoResponse retirarReserva(Long id, RetiradaReservaRequest request) {
        Reserva reserva = buscarReserva(id);
        return emprestimoService.retirarReserva(new EmprestimoRequest(
                reserva.getExemplar().getId(), reserva.getUsuario().getId(),
                request.funcionarioId(), request.dataPrevistaDevolucao(), request.prazo()), id);
    }

    public void apagarReserva(Long id) {
        Reserva reserva = buscarReserva(id);
        exemplarService.bloquear(reserva.getExemplar().getId());
        if (ReservaLancamentoService.EM_ANDAMENTO.contains(reserva.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancele a reserva antes de excluí-la");
        }
        reservaRepository.delete(reserva);
    }

    public void apagarTodos() {
        for (Reserva reserva : reservaRepository.findAll()) apagarReserva(reserva.getId());
    }
}
