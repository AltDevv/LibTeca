package com.libteca.dto.funcionario;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record FuncionarioRequest(
        @NotNull @Positive Long usuarioId,
        @NotNull @PastOrPresent LocalDate dataAdmissao,
        @NotNull Boolean ativo
) {}
