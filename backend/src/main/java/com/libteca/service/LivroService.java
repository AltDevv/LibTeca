package com.libteca.service;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.Autor;
import com.libteca.entity.Categoria;
import com.libteca.entity.Editora;
import com.libteca.entity.Livro;
import com.libteca.filter.LivroFiltro;
import com.libteca.mapper.LivroMapper;
import com.libteca.repository.AutorRepository;
import com.libteca.repository.CategoriaRepository;
import com.libteca.repository.EditoraRepository;
import com.libteca.repository.LivroRepository;
import com.libteca.specification.LivroSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

@Service
public class LivroService {

    private final LivroMapper livroMapper;
    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final EditoraRepository editoraRepository;

    public LivroService(LivroRepository livroRepository, LivroMapper livroMapper,
                         AutorRepository autorRepository, CategoriaRepository categoriaRepository,
                         EditoraRepository editoraRepository) {
        this.livroRepository = livroRepository;
        this.livroMapper = livroMapper;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.editoraRepository = editoraRepository;
    }

    // Todos
    public Page<LivroResponse> listarTodos(Pageable pageable) {

        return livroRepository
                .findAll(pageable)
                .map(livroMapper::toResponse);

    }

    // Todos
    public void apagarTodos() {
        livroRepository.deleteAll();
    }

    // Adicionar
    public LivroResponse adicionarLivro(LivroRequest request) {

        Autor autor = autorRepository.findById(request.autorId())
                .orElseThrow(() -> new RuntimeException("Autor não encontrado"));

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Editora editora = editoraRepository.findById(request.editoraId())
                .orElseThrow(() -> new RuntimeException("Editora não encontrada"));

        Livro livro = livroMapper.toEntity(request, autor, categoria, editora);

        Livro salvo = livroRepository.save(livro);

        return livroMapper.toResponse(salvo);
    }

    // Buscar por ID
    public LivroResponse mostrarLivro(Long id) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        return livroMapper.toResponse(livro);
    }

    // Apagar por ID
    public void apagarLivro(Long id) {

        if (!livroRepository.existsById(id)) {
            throw new RuntimeException("Livro não encontrado");//Criar excessão específica aqui quando possível
        }

        livroRepository.deleteById(id);
    }

    //!Busca por categoria,
    public Page<LivroResponse> buscar(
            LivroFiltro filtro,
            Pageable pageable) {

        Specification<Livro> specification =
                LivroSpecification.comFiltro(filtro);

        Page<Livro> livros =
                livroRepository.findAll(specification, pageable);

        return livros.map(livroMapper::toResponse);
    }


    //public Page<LivroResponse> buscarPorTitulo(String titulo, Pageable pageable) {
    //
    //        return livroRepository
    //                .findByTituloContainingIgnoreCase(titulo, pageable)
    //                .map(livroMapper::toResponse);
    //
    //    }
    //
    //    public Page<LivroResponse> buscarPorAutor(String autor, Pageable pageable) {
    //
    //        return livroRepository
    //                .findByAutorNomeContainingIgnoreCase(autor, pageable)
    //                .map(livroMapper::toResponse);
    //    }
    //
    //    public Page<LivroResponse> buscarPorCategoria(String categoria, Pageable pageable) {
    //
    //        return livroRepository
    //                .findByCategoriaNomeContainingIgnoreCase(categoria, pageable)
    //                .map(livroMapper::toResponse);
    //    }
    //
    //    public Page<LivroResponse> buscarPorEditora(String editora, Pageable pageable) {
    //
    //        return livroRepository
    //                .findByEditoraNomeContainingIgnoreCase(editora, pageable)
    //                .map(livroMapper::toResponse);
    //    }

}
