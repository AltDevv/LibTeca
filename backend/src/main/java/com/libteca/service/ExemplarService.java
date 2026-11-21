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
        if (request.tipo() == TypeLivro.VIRTUAL && (request.caminhoArquivo() == null || request.caminhoArquivo().isBlank())) {
            throw new IllegalArgumentException("O exemplar virtual precisa de um caminho de arquivo");
        }
        if (request.tipo() == TypeLivro.FISICO && (request.codigoPatrimonio() == null || request.codigoPatrimonio().isBlank())) {
            throw new IllegalArgumentException("O exemplar físico precisa de um código de patrimônio");
        }
        if (request.tipo() == TypeLivro.FISICO && request.caminhoArquivo() != null && !request.caminhoArquivo().isBlank()) {
            throw new IllegalArgumentException("O caminho do arquivo pertence ao exemplar virtual");
        }
        Exemplar exemplar = exemplarMapper.toEntity(request, livro);
        if (exemplar.getCodigoPatrimonio() != null && exemplar.getCodigoPatrimonio().isBlank()) exemplar.setCodigoPatrimonio(null);
        if (exemplar.getCaminhoArquivo() != null && exemplar.getCaminhoArquivo().isBlank()) exemplar.setCaminhoArquivo(null);
        if (exemplar.getCodigoPatrimonio() != null && (id == null ? exemplarRepository.existsByCodigoPatrimonio(exemplar.getCodigoPatrimonio())
                : exemplarRepository.existsByCodigoPatrimonioAndIdNot(exemplar.getCodigoPatrimonio(), id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de patrimônio já cadastrado");
        }
        return exemplar;
    }

    public ExemplarResponse adicionarExemplar(ExemplarRequest request) {
        return exemplarMapper.toResponse(exemplarRepository.save(montarExemplar(request, null)));
    }

    public ExemplarResponse atualizarExemplar(Long id, ExemplarRequest request) {
        Exemplar exemplar = bloquear(id);
        if (exemplar.getStatus() == StatusExemplar.EMPRESTADO || exemplar.getStatus() == StatusExemplar.RESERVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conclua o empréstimo ou a reserva antes de editar este exemplar.");
        }
        boolean mudouIdentidade = !exemplar.getLivro().getId().equals(request.livroId()) || exemplar.getTipo() != request.tipo();
        if (mudouIdentidade && (emprestimos.existsByExemplarId(id) || reservas.existsByExemplarId(id))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O livro e o formato não podem ser alterados em um exemplar com histórico.");
        }
        Exemplar dados = montarExemplar(request, id);
        exemplar.setLivro(dados.getLivro()); exemplar.setTipo(dados.getTipo());
        exemplar.setDataAquisicao(dados.getDataAquisicao()); exemplar.setCodigoPatrimonio(dados.getCodigoPatrimonio());
        exemplar.setCaminhoArquivo(dados.getCaminhoArquivo());
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
