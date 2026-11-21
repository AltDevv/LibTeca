package com.libteca.dto.reserva;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record RetiradaReservaRequest(
        @NotNull(message = "A data prevista de devolução é obrigatória")
        @FutureOrPresent LocalDate dataPrevistaDevolucao,
        @Positive Long funcionarioId,
        com.libteca.enums.PrazoEmprestimo prazo
) {
    public RetiradaReservaRequest(LocalDate dataPrevistaDevolucao, Long funcionarioId) {
        this(dataPrevistaDevolucao, funcionarioId, null);
    }
}
