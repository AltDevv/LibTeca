package com.libteca.dto.usuario;

import com.libteca.enums.Role;

public record UsuarioResponse(

        Long id,
        String nome,
        String email,
        Role role

        // senha não é exposta na resposta por segurança

) {}
