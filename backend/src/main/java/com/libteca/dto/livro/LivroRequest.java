package com.libteca.dto.livro;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LivroRequest(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotNull(message = "O autor é obrigatório")
        Long autorId,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "A editora é obrigatória")
        Long editoraId,

        @NotNull
        @Min(0)
        Integer quantidadeDisponivel,

        @NotNull
        @Min(1)
        Integer numeroDePaginas,

        @NotNull
        @Min(1)
        Integer ano

) {}
