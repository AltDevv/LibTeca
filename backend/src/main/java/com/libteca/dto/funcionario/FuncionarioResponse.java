package com.libteca.dto.funcionario;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.libteca.dto.usuario.UsuarioResponse;
public record FuncionarioResponse(
        Long id,
        UsuarioResponse usuario,
        LocalDate dataAdmissao,
        Boolean ativo
) {}
