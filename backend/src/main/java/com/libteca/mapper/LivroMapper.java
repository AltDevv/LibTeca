package com.libteca.mapper;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.Livro;
import org.springframework.stereotype.Component;

@Component
public class LivroMapper {

    public Livro toEntity(LivroRequest request) {

        Livro livro = new Livro();

        livro.setTitulo(request.titulo());
        livro.setAutor(request.autor());
        livro.setCategoria(request.categoria());
        livro.setQuantidadeDisponivel(request.quantidadeDisponivel());
        livro.setNumeroDePaginas(request.numeroDePaginas());
        livro.setAno(request.ano());

        return livro;
    }

    public LivroResponse toResponse(Livro livro) {

        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor(),
                livro.getCategoria(),
                livro.getQuantidadeDisponivel(),
                livro.getNumeroDePaginas(),
                livro.getAno()
        );

    }

}
