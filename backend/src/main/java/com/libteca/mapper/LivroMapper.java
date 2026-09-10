package com.libteca.mapper;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.Autor;
import com.libteca.entity.Categoria;
import com.libteca.entity.Editora;
import com.libteca.entity.Livro;
import com.libteca.mapper.AutorMapper;
import com.libteca.mapper.CategoriaMapper;
import com.libteca.mapper.EditoraMapper;
import org.springframework.stereotype.Component;

@Component
public class LivroMapper {

    private final AutorMapper autorMapper;
    private final CategoriaMapper categoriaMapper;
    private final EditoraMapper editoraMapper;

    public LivroMapper(AutorMapper autorMapper, CategoriaMapper categoriaMapper, EditoraMapper editoraMapper) {
        this.autorMapper = autorMapper;
        this.categoriaMapper = categoriaMapper;
        this.editoraMapper = editoraMapper;
    }

    public Livro toEntity(LivroRequest request, Autor autor, Categoria categoria, Editora editora) {

        Livro livro = new Livro();

        livro.setTitulo(request.titulo());
        livro.setAutor(autor);
        livro.setCategoria(categoria);
        livro.setEditora(editora);
        livro.setQuantidadeDisponivel(request.quantidadeDisponivel());
        livro.setNumeroDePaginas(request.numeroDePaginas());
        livro.setAno(request.ano());

        return livro;
    }

    public LivroResponse toResponse(Livro livro) {

        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                autorMapper.toResponse(livro.getAutor()),
                categoriaMapper.toResponse(livro.getCategoria()),
                editoraMapper.toResponse(livro.getEditora()),
                livro.getQuantidadeDisponivel(),
                livro.getNumeroDePaginas(),
                livro.getAno()
        );

    }

}
