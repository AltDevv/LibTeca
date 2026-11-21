package com.libteca.dto.telefone;

import jakarta.validation.constraints.*;

public record TelefoneResponse(
        Long id,
        Long usuarioId,
        String numero
) {}
