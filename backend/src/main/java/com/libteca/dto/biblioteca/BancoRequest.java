package com.libteca.dto.biblioteca;

import jakarta.validation.constraints.*;

public record BancoRequest(
        @NotBlank @Pattern(regexp="sqlite|mysql") String tipo,
        @NotBlank @Size(max=128) String codigo,
        @Size(max=253) String host,
        @Min(1) @Max(65535) Integer porta,
        @Size(max=64) String banco,
        @Size(max=100) String usuario,
        @Size(max=255) String senha) {}
