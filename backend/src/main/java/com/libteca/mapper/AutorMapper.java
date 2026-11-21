package com.libteca.mapper;

import com.libteca.dto.autor.AutorRequest;
import com.libteca.dto.autor.AutorResponse;
import com.libteca.entity.Autor;
import org.springframework.stereotype.Component;

@Component
public class AutorMapper {

    public Autor toEntity(AutorRequest request) {

        Autor autor = new Autor();

        autor.setNome(request.nome());

        return autor;
    }

    public AutorResponse toResponse(Autor autor) {

        return new AutorResponse(
                autor.getId(),
                autor.getNome()
        );

    }

}
