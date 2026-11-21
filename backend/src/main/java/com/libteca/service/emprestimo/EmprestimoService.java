package com.libteca.service.emprestimo;

import com.libteca.dto.emprestimo.*;
import com.libteca.entity.*;
import com.libteca.enums.*;
import com.libteca.handler.emprestimo.exception.*;
import com.libteca.handler.livro.exception.LivroIndisponivelException;
import com.libteca.handler.usuario.exception.UsuarioNaoEncontradoException;
import com.libteca.mapper.EmprestimoMapper;
import com.libteca.repository.*;
import com.libteca.service.ExemplarService;
import com.libteca.service.FuncionarioService;
import com.libteca.service.reserva.ReservaLancamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final EmprestimoMapper emprestimoMapper;
    private final ExemplarService exemplarService;
    private final FuncionarioService funcionarioService;
    private final ReservaLancamentoService reservaLancamentoService;

    public EmprestimoService(EmprestimoRepository emprestimoRepository, UsuarioRepository usuarioRepository,
                             ReservaRepository reservaRepository, EmprestimoMapper emprestimoMapper,
                             ExemplarService exemplarService, FuncionarioService funcionarioService,
                             ReservaLancamentoService reservaLancamentoService) {
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.emprestimoMapper = emprestimoMapper;
        this.exemplarService = exemplarService;
        this.funcionarioService = funcionarioService;
        this.reservaLancamentoService = reservaLancamentoService;
    }

    public Page<EmprestimoResponse> listarTodos(Pageable pageable) {
        return emprestimoRepository.findAll(pageable).map(emprestimoMapper::toResponse);
    }

    public EmprestimoResponse mostrarEmprestimo(Long id) {
        return emprestimoMapper.toResponse(buscarEmprestimo(id));
    }

    private Emprestimo buscarEmprestimo(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public EmprestimoResponse adicionarEmprestimo(EmprestimoRequest request) {
        return registrarEmprestimo(request, null);
    }

    public EmprestimoResponse retirarReserva(EmprestimoRequest request, Long reservaId) {
        return registrarEmprestimo(request, reservaId);
    }

    private EmprestimoResponse registrarEmprestimo(EmprestimoRequest request, Long reservaId) {
        validarPrazo(request.dataPrevistaDevolucao());
        Exemplar exemplar = exemplarService.bloquear(request.exemplarId());
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        Funcionario funcionario = funcionarioService.buscarAtivo(request.funcionarioId());
        if (exemplar.getLivro().getDataLancamento().isAfter(LocalDate.now())) {
            throw new LivroIndisponivelException("O livro ainda não foi lançado");
        }
        if (exemplar.getStatus() == StatusExemplar.INDISPONIVEL
                || !emprestimoRepository.findByExemplarIdAndDataDevolucaoIsNull(exemplar.getId()).isEmpty()) {
            throw new LivroIndisponivelException("Este exemplar não está disponível");
        }
        List<Reserva> reservas = reservaRepository.findByExemplarIdAndStatusInOrderByDataReservaAscIdAsc(
                exemplar.getId(), ReservaLancamentoService.EM_ANDAMENTO);
        if (reservaId == null) {
            if (!reservas.isEmpty() || exemplar.getStatus() != StatusExemplar.DISPONIVEL) {
                throw new LivroIndisponivelException("Este exemplar está reservado ou emprestado");
            }
        } else {
            Reserva reserva = reservas.stream().filter(r -> r.getId().equals(reservaId)).findFirst()
                    .orElseThrow(() -> new LivroIndisponivelException("A reserva não está mais em andamento"));
            if (reserva.getStatus() != StatusReserva.DISPONIVEL_PARA_RETIRADA
                    || !reserva.getUsuario().getId().equals(usuario.getId())) {
                throw new LivroIndisponivelException("A reserva não está disponível para este usuário");
            }
            reserva.setStatus(StatusReserva.CONCLUIDA);
        }
        Emprestimo emprestimo = emprestimoMapper.toEntity(exemplar, usuario, funcionario, request.dataPrevistaDevolucao());
        exemplar.setStatus(StatusExemplar.EMPRESTADO);
        return emprestimoMapper.toResponse(emprestimoRepository.save(emprestimo));
    }

    private void validarPrazo(LocalDate prazo) {
        if (prazo == null || prazo.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Informe uma data prevista de devolução igual ou posterior a hoje");
        }
    }

    public EmprestimoResponse devolverEmprestimo(Long id) {
        Long exemplarId = buscarEmprestimo(id).getExemplar().getId();
        Exemplar exemplar = exemplarService.bloquear(exemplarId);
        Emprestimo emprestimo = emprestimoRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException("Empréstimo não encontrado"));
        exemplarService.recarregar(emprestimo);
        if (emprestimo.getDataDevolucao() != null || emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new EmprestimoJaDevolvidoException("Este empréstimo já foi devolvido");
        }
        // Físicos e virtuais seguem a mesma regra de devolução.
        emprestimo.setDataDevolucao(LocalDate.now());
        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimoRepository.flush();
        reservaLancamentoService.atualizarDisponibilidade(exemplar);
        return emprestimoMapper.toResponse(emprestimo);
    }

    public void processarEmprestimosExpirados() {
        for (Emprestimo candidato : emprestimoRepository.findByStatusAndDataExpiracaoBefore(StatusEmprestimo.ATIVO, LocalDate.now())) {
            exemplarService.bloquear(candidato.getExemplar().getId());
            Emprestimo emprestimo = emprestimoRepository.buscarParaAtualizar(candidato.getId()).orElseThrow();
            exemplarService.recarregar(emprestimo);
            if (emprestimo.getDataDevolucao() == null && emprestimo.getStatus() == StatusEmprestimo.ATIVO) {
                // Vencimento não significa devolução e não libera o exemplar.
                emprestimo.setStatus(StatusEmprestimo.EXPIRADO);
            }
        }
    }

    public void apagarEmprestimo(Long id) {
        Emprestimo emprestimo = buscarEmprestimo(id);
        exemplarService.bloquear(emprestimo.getExemplar().getId());
        if (emprestimo.getDataDevolucao() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registre a devolução antes de excluir o empréstimo");
        }
        emprestimoRepository.delete(emprestimo);
    }

    public void apagarTodos() {
        for (Emprestimo emprestimo : emprestimoRepository.findAll()) apagarEmprestimo(emprestimo.getId());
    }
}
