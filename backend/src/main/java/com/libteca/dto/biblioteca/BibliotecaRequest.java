package com.libteca.dto.biblioteca;

import jakarta.validation.constraints.*;

public record BibliotecaRequest(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 255) @Pattern(regexp = "https?://[^\\s]+", message = "Informe um endereço começando com http:// ou https://") String site
) {}
