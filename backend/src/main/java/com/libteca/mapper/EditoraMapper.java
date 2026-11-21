package com.libteca.mapper;

import com.libteca.dto.editora.EditoraRequest;
import com.libteca.dto.editora.EditoraResponse;
import com.libteca.entity.Editora;
import org.springframework.stereotype.Component;

@Component
public class EditoraMapper {

    public Editora toEntity(EditoraRequest request) {

        Editora editora = new Editora();

        editora.setNome(request.nome());

        return editora;
    }

    public EditoraResponse toResponse(Editora editora) {

        return new EditoraResponse(
                editora.getId(),
                editora.getNome()
        );

    }

}
