package com.libteca.dto.reserva;

import jakarta.validation.constraints.*;

public record ReservaRequest(
        @NotNull @Positive Long exemplarId,
        @NotNull @Positive Long usuarioId,
        @Positive Long funcionarioId
) {}
