package com.libteca.service;

import jakarta.persistence.LockModeType;

import com.libteca.dto.exemplar.*;
import com.libteca.entity.*;
import com.libteca.enums.*;
import com.libteca.mapper.ExemplarMapper;
import com.libteca.repository.*;
import com.libteca.handler.livro.exception.LivroNaoEncontradoException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Transactional
public class ExemplarService {

    private final ExemplarRepository exemplarRepository;
    private final LivroRepository livroRepository;
    private final ExemplarMapper exemplarMapper;
    private final EntityManager entityManager;
    private final EmprestimoRepository emprestimos;
    private final ReservaRepository reservas;

    public ExemplarService(ExemplarRepository exemplarRepository, LivroRepository livroRepository,
                           ExemplarMapper exemplarMapper, EntityManager entityManager,
                           EmprestimoRepository emprestimos, ReservaRepository reservas) {
        this.emprestimos = emprestimos; this.reservas = reservas;
        this.exemplarRepository = exemplarRepository;
        this.livroRepository = livroRepository;
        this.exemplarMapper = exemplarMapper;
        this.entityManager = entityManager;
    }

    public List<ExemplarResponse> listarTodos() {
        return exemplarRepository.findAll().stream().map(exemplarMapper::toResponse).toList();
    }

    public List<ExemplarResponse> buscarPorLivro(Long livroId) {
        return exemplarRepository.findByLivroId(livroId).stream().map(exemplarMapper::toResponse).toList();
    }

    public ExemplarResponse mostrarExemplar(Long id) {
        return exemplarMapper.toResponse(buscarExemplar(id));
    }

    private Exemplar buscarExemplar(Long id) {
        return exemplarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exemplar não encontrado"));
    }

    // Todas as mudanças de disponibilidade passam por esta escrita, na mesma transação.
    public Exemplar bloquear(Long id) {
        if (exemplarRepository.bloquear(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exemplar não encontrado");
        }
        Exemplar exemplar = buscarExemplar(id);
        entityManager.refresh(exemplar);
        return exemplar;
    }

    public void recarregar(Object entidade) {
        entityManager.refresh(entidade, LockModeType.PESSIMISTIC_WRITE);
    }

    private Exemplar montarExemplar(ExemplarRequest request, Long id) {
        Livro livro = livroRepository.findById(request.livroId())
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));
        if (request.tipo() == TypeLivro.VIRTUAL && request.codigoPatrimonio() != null && !request.codigoPatrimonio().isBlank())
            throw new IllegalArgumentException("O código de patrimônio pertence ao exemplar físico");
        if (request.tipo() == TypeLivro.FISICO && (request.codigoPatrimonio() == null || request.codigoPatrimonio().isBlank())) {
            throw new IllegalArgumentException("O exemplar físico precisa de um código de patrimônio");
        }
        if (request.tipo() == TypeLivro.FISICO && request.caminhoArquivo() != null && !request.caminhoArquivo().isBlank()) {
            throw new IllegalArgumentException("O caminho do arquivo pertence ao exemplar virtual");
        }
        Exemplar exemplar = exemplarMapper.toEntity(request, livro);
        if (exemplar.getCodigoPatrimonio() != null && exemplar.getCodigoPatrimonio().isBlank()) exemplar.setCodigoPatrimonio(null);
        if (exemplar.getCaminhoArquivo() != null && exemplar.getCaminhoArquivo().isBlank()) exemplar.setCaminhoArquivo(null);
        if (exemplar.getTipo() == TypeLivro.VIRTUAL && exemplar.getCaminhoArquivo() == null)
            exemplar.setStatus(StatusExemplar.INDISPONIVEL);
        if (exemplar.getCodigoPatrimonio() != null && (id == null ? exemplarRepository.existsByCodigoPatrimonio(exemplar.getCodigoPatrimonio())
                : exemplarRepository.existsByCodigoPatrimonioAndIdNot(exemplar.getCodigoPatrimonio(), id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de patrimônio já cadastrado");
        }
        return exemplar;
    }

    public ExemplarResponse adicionarExemplar(ExemplarRequest request) {
        if (request.tipo() == TypeLivro.VIRTUAL) {
            livroRepository.bloquearCadastro(request.livroId());
            if (exemplarRepository.buscarPorLivroParaAtualizar(request.livroId()).stream().anyMatch(e -> e.getTipo() == TypeLivro.VIRTUAL))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este livro já possui um exemplar virtual. Edite o existente");
        }
        return exemplarMapper.toResponse(exemplarRepository.save(montarExemplar(request, null)));
    }

    // Um único exemplar virtual atende a todos os leitores simultaneamente.
    public List<ExemplarResponse> adicionarVirtuais(ExemplarRequest request) {
        if (request.tipo() != TypeLivro.VIRTUAL) throw new IllegalArgumentException("Informe o formato digital");
        List<ExemplarResponse> resultado = new ArrayList<>();
        resultado.add(adicionarExemplar(request));
        return resultado;
    }

    public Exemplar bloquearVirtualDisponivel(Long id) {
        Exemplar referencia = buscarExemplar(id);
        if (referencia.getTipo() != TypeLivro.VIRTUAL)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Retire o exemplar físico com um funcionário");
        Exemplar exemplar = bloquear(id);
        recarregar(exemplar);
        if (exemplar.getStatus() == StatusExemplar.INDISPONIVEL || exemplar.getCaminhoArquivo() == null || exemplar.getCaminhoArquivo().isBlank())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O arquivo digital ainda não está disponível");
        return exemplar;
    }
    public ExemplarResponse atualizarExemplar(Long id, ExemplarRequest request) {
        Exemplar exemplar = bloquear(id);
        if (request.tipo() == TypeLivro.VIRTUAL && (!exemplar.getLivro().getId().equals(request.livroId()) || exemplar.getTipo() != TypeLivro.VIRTUAL))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cadastre o virtual separadamente, sem transformar outro exemplar");
        if (!emprestimos.buscarEmAndamentoPorExemplar(id).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conclua os empréstimos antes de editar este exemplar");
        if (exemplar.getStatus() == StatusExemplar.EMPRESTADO || exemplar.getStatus() == StatusExemplar.RESERVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conclua o empréstimo ou a reserva antes de editar este exemplar.");
        }
        boolean mudouIdentidade = !exemplar.getLivro().getId().equals(request.livroId()) || exemplar.getTipo() != request.tipo();
        if (mudouIdentidade && (emprestimos.existsByExemplarId(id) || reservas.existsByExemplarId(id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O livro e o formato não podem ser alterados em um exemplar com histórico.");
        }
        Exemplar dados = montarExemplar(request, id);
        boolean aguardavaArquivo = exemplar.getTipo() == TypeLivro.VIRTUAL && exemplar.getCaminhoArquivo() == null;
        exemplar.setLivro(dados.getLivro()); exemplar.setTipo(dados.getTipo());
        exemplar.setDataAquisicao(dados.getDataAquisicao()); exemplar.setCodigoPatrimonio(dados.getCodigoPatrimonio());
        exemplar.setCaminhoArquivo(dados.getCaminhoArquivo());
        if (exemplar.getTipo() == TypeLivro.VIRTUAL && exemplar.getCaminhoArquivo() == null)
            exemplar.setStatus(StatusExemplar.INDISPONIVEL);
        else if (aguardavaArquivo && exemplar.getStatus() == StatusExemplar.INDISPONIVEL)
            exemplar.setStatus(StatusExemplar.DISPONIVEL);
        return exemplarMapper.toResponse(exemplarRepository.save(exemplar));
    }

    public void apagarExemplar(Long id) {
        Exemplar exemplar = bloquear(id);
        if (exemplar.getStatus() == StatusExemplar.EMPRESTADO || exemplar.getStatus() == StatusExemplar.RESERVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O exemplar possui uma operação em andamento");
        }
        exemplarRepository.delete(exemplar);
        exemplarRepository.flush();
    }

    public void apagarTodos() {
        for (Exemplar exemplar : exemplarRepository.findAll()) apagarExemplar(exemplar.getId());
    }
}
