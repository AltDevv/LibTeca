package com.libteca.dto.biblioteca;

import jakarta.validation.constraints.*;
public record TransferenciaRequest(@NotNull @Positive Long funcionarioId, @NotBlank String senhaAtual) {}
