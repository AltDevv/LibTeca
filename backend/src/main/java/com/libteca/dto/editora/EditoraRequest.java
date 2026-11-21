package com.libteca.dto.editora;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;

public record EditoraRequest(

        @NotBlank(message = "O nome da editora é obrigatório")
        @Size(max = 45)
        String nome

) {}
