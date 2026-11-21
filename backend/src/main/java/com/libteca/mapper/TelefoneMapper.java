package com.libteca.mapper;

import com.libteca.dto.telefone.*;
import com.libteca.entity.*;
import org.springframework.stereotype.Component;

@Component
public class TelefoneMapper {

    public Telefone toEntity(TelefoneRequest request, Usuario usuario) {
        Telefone telefone = new Telefone();
        telefone.setUsuario(usuario);
        telefone.setNumero(request.numero());
        return telefone;
    }

    public TelefoneResponse toResponse(Telefone telefone) {
        return new TelefoneResponse(telefone.getId(), telefone.getUsuario().getId(), telefone.getNumero());
    }
}
