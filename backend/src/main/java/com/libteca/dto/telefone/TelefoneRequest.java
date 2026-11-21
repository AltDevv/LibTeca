package com.libteca.dto.telefone;

import jakarta.validation.constraints.*;

public record TelefoneRequest(
        @NotNull @Positive Long usuarioId,
        @NotBlank @Pattern(regexp = "[0-9]{10,11}", message = "Informe o DDD e o telefone, com 10 ou 11 dígitos") String numero
) {}
