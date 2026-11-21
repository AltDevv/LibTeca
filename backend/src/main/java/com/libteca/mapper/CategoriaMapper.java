package com.libteca.mapper;

import com.libteca.dto.categoria.CategoriaRequest;
import com.libteca.dto.categoria.CategoriaResponse;
import com.libteca.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toEntity(CategoriaRequest request) {

        Categoria categoria = new Categoria();

        categoria.setNome(request.nome());

        return categoria;
    }

    public CategoriaResponse toResponse(Categoria categoria) {

        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome()
        );

    }

}
