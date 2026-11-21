package com.libteca.dto.biblioteca;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record InstalacaoRequest(
        @NotNull @Valid BibliotecaRequest biblioteca,
        @NotBlank @Size(max = 128) String codigo,
        @NotBlank @Size(max = 70) String nomeAdministrador,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 60) String senha
) {}
