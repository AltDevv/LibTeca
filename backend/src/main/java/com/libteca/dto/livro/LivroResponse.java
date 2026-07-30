package com.libteca.dto.livro;

public record LivroResponse(

        Long id,
        String titulo,
        String autor,
        String categoria,
        Integer quantidadeDisponivel,
        Integer numeroDePaginas,
        Integer ano

) {}