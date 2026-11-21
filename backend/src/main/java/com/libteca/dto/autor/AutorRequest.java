package com.libteca.dto.autor;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;

public record AutorRequest(

        @NotBlank(message = "O nome do autor é obrigatório")
        @Size(max = 45)
        String nome

) {}
