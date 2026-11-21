package com.libteca.dto.biblioteca;

import jakarta.validation.constraints.*;
public record ExclusaoBibliotecaRequest(@NotBlank String nomeBiblioteca, @NotBlank String senhaAtual) {}
