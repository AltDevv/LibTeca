package com.libteca.dto.emprestimo;

import com.libteca.enums.PrazoEmprestimo;
import jakarta.validation.constraints.*;
public record NovoEmprestimoRequest(@NotNull @Positive Long exemplarId,
        @NotNull @Positive Long usuarioId, @NotNull PrazoEmprestimo prazo) {}
