package com.libteca.dto.emprestimo;

import com.libteca.dto.livro.LivroResponse;
import com.libteca.dto.usuario.UsuarioResponse;

import java.time.LocalDate;

public record EmprestimoResponse(
        Long id,
        LivroResponse livro,
        UsuarioResponse usuario,
        LocalDate dataEmprestimo,
        LocalDate dataExpiracao,
        LocalDate dataDevolucao
) {}
