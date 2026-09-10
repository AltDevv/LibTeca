package com.libteca.dto.livro;

import com.libteca.dto.autor.AutorResponse;
import com.libteca.dto.categoria.CategoriaResponse;
import com.libteca.dto.editora.EditoraResponse;

public record LivroResponse(

        Long id,
        String titulo,
        AutorResponse autor,
        CategoriaResponse categoria,
        EditoraResponse editora,
        Integer quantidadeDisponivel,
        Integer numeroDePaginas,
        Integer ano

) {}
