package com.libteca.dto.emprestimo;

import com.libteca.enums.PrazoEmprestimo;
import jakarta.validation.constraints.NotNull;
public record PrazoRequest(@NotNull(message = "Escolha 1 semana, 1 mês ou 2 meses") PrazoEmprestimo prazo) {}
