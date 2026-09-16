package com.libteca.dto.livro;

import com.libteca.dto.autor.AutorResponse;
import com.libteca.dto.categoria.CategoriaResponse;
import com.libteca.dto.editora.EditoraResponse;
import com.libteca.enums.TypeLivro;

import java.time.LocalDate;

public record LivroResponse(

        Long id,
        String titulo,

        AutorResponse autor,
        CategoriaResponse categoria,
        EditoraResponse editora,

        //!Tipo
        TypeLivro tipo,

        //!Físico
        Integer quantidadeDisponivel,

        //!Dados gerais
        Integer numeroDePaginas,
        Integer ano,

        //!Virtual
        Boolean permiteDownload,
        Long tamanhoMaximoArquivo,
        Boolean possuiExpiracao,
        Integer diasExpiracao,
        Integer quantidadeAcessosDisponiveis,

        //!Lançamento
        Boolean possuiPrelancamento,
        LocalDate dataLancamento

) {}