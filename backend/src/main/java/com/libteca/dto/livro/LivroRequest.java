package com.libteca.dto.livro;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LivroRequest(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotBlank(message = "O autor é obrigatório")
        String autor,

        @NotBlank(message = "A categoria é obrigatória")
        String categoria,

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