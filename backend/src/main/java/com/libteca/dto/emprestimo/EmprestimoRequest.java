package com.libteca.dto.emprestimo;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EmprestimoRequest(

        @NotNull(message = "O livro é obrigatório")
        Long livroId,

        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "A data prevista de devolução é obrigatória")
        LocalDate dataPrevistaDevolucao

) {}
