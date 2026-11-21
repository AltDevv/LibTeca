package com.libteca.service;

import com.libteca.dto.livro.*;
import com.libteca.entity.*;
import com.libteca.filter.LivroFiltro;
import com.libteca.handler.livro.exception.*;
import com.libteca.mapper.LivroMapper;
import com.libteca.repository.*;
import com.libteca.specification.LivroSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashSet;

@Service
@Transactional
public class LivroService {

    private final BibliotecaRepository bibliotecaRepository;
    private final LivroMapper livroMapper;
    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final EditoraRepository editoraRepository;
    private final FuncionarioService funcionarioService;
    private final ExemplarRepository exemplarRepository;

    public LivroService(LivroRepository livroRepository, LivroMapper livroMapper, AutorRepository autorRepository,
                        CategoriaRepository categoriaRepository, EditoraRepository editoraRepository,
                        FuncionarioService funcionarioService, BibliotecaRepository bibliotecaRepository,
                        ExemplarRepository exemplarRepository) {
        this.exemplarRepository = exemplarRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.livroRepository = livroRepository;
        this.livroMapper = livroMapper;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.editoraRepository = editoraRepository;
        this.funcionarioService = funcionarioService;
    }

    public Page<LivroResponse> listarTodos(Pageable pageable) {
        return livroRepository.findAll(pageable).map(livroMapper::toResponse);
    }

    private Livro montarLivro(LivroRequest request, Funcionario funcionario) {
        LinkedHashSet<Autor> autores = new LinkedHashSet<>();
        for (Long id : request.autoresIds()) {
            autores.add(autorRepository.findById(id)
                    .orElseThrow(() -> new AutorNaoEncontradoException("Autor não encontrado: " + id)));
        }
        LinkedHashSet<Categoria> categorias = new LinkedHashSet<>();
        for (Long id : request.categoriasIds()) {
            categorias.add(categoriaRepository.findById(id)
                    .orElseThrow(() -> new CategoriaNaoEncontradaException("Categoria não encontrada: " + id)));
        }
        Editora editora = editoraRepository.findById(request.editoraId())
                .orElseThrow(() -> new EditoraNaoEncontradaException("Editora não encontrada"));
        Livro livro = livroMapper.toEntity(request, autores, categorias, editora, funcionario);
        return livro;
    }

    public LivroResponse adicionarLivro(LivroRequest request) {
        bibliotecaRepository.bloquear();
        Livro salvo = livroRepository.save(montarLivro(request, funcionarioService.buscarAtivo(request.funcionarioId())));
        bibliotecaRepository.iniciarAcervo();
        return livroMapper.toResponse(salvo);
    }

    public LivroResponse atualizarLivro(Long id, LivroRequest request) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));
        Livro dados = montarLivro(request, livro.getFuncionario());
        livro.setTitulo(dados.getTitulo()); livro.setCapa(dados.getCapa());
        livro.setNumeroDePaginas(dados.getNumeroDePaginas()); livro.setDataLancamento(dados.getDataLancamento());
        livro.setEditora(dados.getEditora());
        if (request.limiteReservas() != null) livro.setLimiteReservas(dados.getLimiteReservas());
        livro.getAutores().clear(); livro.getAutores().addAll(dados.getAutores());
        livro.getCategorias().clear(); livro.getCategorias().addAll(dados.getCategorias());
        return livroMapper.toResponse(livroRepository.save(livro));
    }

    public LivroResponse mostrarLivro(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));
        return livroMapper.toResponse(livro);
    }

    public void apagarLivro(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));
        if (exemplarRepository.countByLivroId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este livro possui exemplares vinculados e não pode ser excluído.");
        }
        livroRepository.delete(livro);
        livroRepository.flush();
    }

    public void apagarTodos() {
        livroRepository.deleteAll();
        livroRepository.flush();
    }

    public Page<LivroResponse> buscar(LivroFiltro filtro, Pageable pageable) {
        return livroRepository.findAll(LivroSpecification.comFiltro(filtro), pageable).map(livroMapper::toResponse);
    }
}
