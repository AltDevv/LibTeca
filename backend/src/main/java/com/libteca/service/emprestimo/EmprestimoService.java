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

    public EmprestimoResponse solicitarEmprestimoFisico(EmprestimoRequest request) {
        return registrarEmprestimo(request, null, true);
    }

    public EmprestimoResponse adicionarEmprestimoDigital(EmprestimoRequest request) {
        Exemplar exemplar = exemplarService.bloquearVirtualDisponivel(request.exemplarId());
        return registrarEmprestimo(new EmprestimoRequest(exemplar.getId(), request.usuarioId(),
                request.funcionarioId(), request.dataPrevistaDevolucao(), request.prazo()), null);
    }

    public EmprestimoResponse retirarReserva(EmprestimoRequest request, Long reservaId) {
        return registrarEmprestimo(request, reservaId);
    }

    private EmprestimoResponse registrarEmprestimo(EmprestimoRequest request, Long reservaId) {
        return registrarEmprestimo(request, reservaId, false);
    }

    private EmprestimoResponse registrarEmprestimo(EmprestimoRequest request, Long reservaId, boolean aguardarRecebimento) {
        validarPrazo(request.dataPrevistaDevolucao());
        Exemplar exemplar = exemplarService.bloquear(request.exemplarId());
        exemplarService.recarregar(exemplar);
        boolean virtual = exemplar.getTipo() == TypeLivro.VIRTUAL;
        if (aguardarRecebimento && exemplar.getTipo() != TypeLivro.FISICO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente o exemplar físico aguarda recebimento");
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        Funcionario funcionario = funcionarioService.buscarAtivo(request.funcionarioId());
        if (!aguardarRecebimento && exemplar.getTipo() == TypeLivro.FISICO && funcionario == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Informe o responsável pela entrega do livro físico");
        if (exemplar.getLivro().getDataLancamento().isAfter(LocalDate.now())) {
            throw new LivroIndisponivelException("O livro ainda não foi lançado");
        }
        if (exemplar.getStatus() == StatusExemplar.INDISPONIVEL
                || (exemplar.getTipo() == TypeLivro.VIRTUAL && (exemplar.getCaminhoArquivo() == null || exemplar.getCaminhoArquivo().isBlank()))
                || (!virtual && !emprestimoRepository.buscarEmAndamentoPorExemplar(exemplar.getId()).isEmpty())) {
            throw new LivroIndisponivelException("Este exemplar não está disponível");
        }
        List<Reserva> reservas = reservaRepository.findByExemplarIdAndStatusInOrderByDataReservaAscIdAsc(
                exemplar.getId(), ReservaLancamentoService.EM_ANDAMENTO);
        if (reservaId == null) {
            if (!virtual && (!reservas.isEmpty() || exemplar.getStatus() != StatusExemplar.DISPONIVEL)) {
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
        emprestimo.setPrazo(request.prazo());
        if (virtual && emprestimoRepository.buscarEmAndamentoPorExemplar(exemplar.getId()).stream()
                .anyMatch(e -> e.getUsuario().getId().equals(usuario.getId()) && e.getStatus() == StatusEmprestimo.ATIVO
                        && !e.getDataExpiracao().isBefore(LocalDate.now())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já possui uma leitura ativa deste livro");
        if (aguardarRecebimento) emprestimo.setStatus(StatusEmprestimo.PENDENTE);
        exemplar.setStatus(virtual ? StatusExemplar.DISPONIVEL : aguardarRecebimento ? StatusExemplar.RESERVADO : StatusExemplar.EMPRESTADO);
        return emprestimoMapper.toResponse(emprestimoRepository.save(emprestimo));
    }

    public EmprestimoResponse confirmarRecebimento(Long id, Long funcionarioId) {
        Funcionario funcionario = funcionarioService.buscarAtivo(funcionarioId);
        if (funcionario == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Informe o responsável pela entrega");
        Exemplar exemplar = exemplarService.bloquear(buscarEmprestimo(id).getExemplar().getId());
        Emprestimo emprestimo = emprestimoRepository.buscarParaAtualizar(id).orElseThrow();
        exemplarService.recarregar(emprestimo);
        validarRecebimentoPendente(emprestimo);
        // Enquanto pendente, as datas são uma previsão. O período efetivo começa no recebimento.
        // A condição na escrita também protege contra uma leitura anterior à transação concorrente.
        LocalDate hoje = LocalDate.now();
        if (emprestimoRepository.confirmarRecebimento(id, hoje, emprestimo.getPrazo() == null ? hoje.plusDays(14) : emprestimo.getPrazo().calcular(hoje)) != 1)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O recebimento já foi confirmado ou cancelado");
        exemplarService.recarregar(emprestimo);
        emprestimo.setFuncionario(funcionario);
        exemplar.setStatus(StatusExemplar.EMPRESTADO);
        return emprestimoMapper.toResponse(emprestimo);
    }

    public EmprestimoResponse cancelarSolicitacao(Long id) {
        Exemplar exemplar = exemplarService.bloquear(buscarEmprestimo(id).getExemplar().getId());
        Emprestimo emprestimo = emprestimoRepository.buscarParaAtualizar(id).orElseThrow();
        exemplarService.recarregar(emprestimo);
        validarRecebimentoPendente(emprestimo);
        if (emprestimoRepository.cancelarSolicitacao(id) != 1)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O recebimento já foi confirmado ou cancelado");
        exemplarService.recarregar(emprestimo);
        // Cancelamento não registra uma devolução que não aconteceu.
        emprestimoRepository.flush();
        reservaLancamentoService.atualizarDisponibilidade(exemplar);
        return emprestimoMapper.toResponse(emprestimo);
    }

    private void validarRecebimentoPendente(Emprestimo emprestimo) {
        if (emprestimo.getStatus() != StatusEmprestimo.PENDENTE || emprestimo.getDataDevolucao() != null
                || emprestimo.getExemplar().getTipo() != TypeLivro.FISICO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este empréstimo não está aguardando recebimento");
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
        if (emprestimo.getStatus() != StatusEmprestimo.ATIVO && emprestimo.getStatus() != StatusEmprestimo.EXPIRADO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente um empréstimo recebido pode ser devolvido");
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
        if (emprestimo.getDataDevolucao() == null && emprestimo.getStatus() != StatusEmprestimo.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registre a devolução antes de excluir o empréstimo");
        }
        emprestimoRepository.delete(emprestimo);
    }

    public void apagarTodos() {
        for (Emprestimo emprestimo : emprestimoRepository.findAll()) apagarEmprestimo(emprestimo.getId());
    }
}
