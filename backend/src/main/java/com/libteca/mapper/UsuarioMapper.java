package com.libteca.mapper;

import com.libteca.dto.usuario.UsuarioRequest;
import com.libteca.dto.usuario.UsuarioResponse;
import com.libteca.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequest request) {

        Usuario usuario = new Usuario();

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());

        return usuario;
    }

    public UsuarioResponse toResponse(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole()
        );

    }

}
