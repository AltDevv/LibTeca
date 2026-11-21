package com.libteca.dto.emprestimo;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record EmprestimoRequest(
        @NotNull @Positive Long exemplarId,
        @NotNull @Positive Long usuarioId,
        @Positive Long funcionarioId,
        @NotNull(message = "A data prevista de devolução é obrigatória")
        @FutureOrPresent LocalDate dataPrevistaDevolucao,
        com.libteca.enums.PrazoEmprestimo prazo
) {
    public EmprestimoRequest(Long exemplarId, Long usuarioId, Long funcionarioId, LocalDate dataPrevistaDevolucao) {
        this(exemplarId, usuarioId, funcionarioId, dataPrevistaDevolucao, null);
    }
}
