package com.libteca.dto.emprestimo;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.libteca.dto.exemplar.ExemplarResponse;
import com.libteca.dto.usuario.UsuarioResponse;
import com.libteca.enums.StatusEmprestimo;
public record EmprestimoResponse(
        Long id,
        ExemplarResponse exemplar,
        UsuarioResponse usuario,
        Long funcionarioId,
        LocalDate dataEmprestimo,
        LocalDate dataExpiracao,
        LocalDate dataDevolucao,
        StatusEmprestimo status,
        com.libteca.enums.PrazoEmprestimo prazo
) {}
