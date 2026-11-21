package com.libteca.dto.categoria;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(

        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(max = 45)
        String nome

) {}
