package com.libteca.mapper;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.Autor;
import com.libteca.entity.Categoria;
import com.libteca.entity.Editora;
import com.libteca.entity.Livro;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Component;

@Component
public class LivroMapper {

    private final AutorMapper autorMapper;
    private final CategoriaMapper categoriaMapper;
    private final EditoraMapper editoraMapper;

    public LivroMapper(
            AutorMapper autorMapper,
            CategoriaMapper categoriaMapper,
            EditoraMapper editoraMapper) {

        this.autorMapper = autorMapper;
        this.categoriaMapper = categoriaMapper;
        this.editoraMapper = editoraMapper;
    }

    public Livro toEntity(
            LivroRequest request,
            Autor autor,
            Categoria categoria,
            Editora editora) {

        Livro livro = new Livro();

        livro.setTitulo(request.titulo());
        livro.setAutor(autor);
        livro.setCategoria(categoria);
        livro.setEditora(editora);

        //!Físico
        livro.setQuantidadeDisponivel(
                request.quantidadeDisponivel()
        );

        //!Dados gerais
        livro.setNumeroDePaginas(
                request.numeroDePaginas()
        );

        livro.setAno(
                request.ano()
        );

        //!Tipo
        livro.setTipo(
                request.tipo()
        );

        //!Virtual
        livro.setPermiteDownload(
                request.permiteDownload()
        );

        livro.setTamanhoMaximoArquivo(
                request.tamanhoMaximoArquivo()
        );

        livro.setPossuiExpiracao(
                request.possuiExpiracao()
        );

        livro.setDiasExpiracao(
                request.diasExpiracao()
        );

        livro.setQuantidadeAcessosDisponiveis(
                request.quantidadeAcessosDisponiveis()
        );

        // Lançamento
        livro.setPossuiPrelancamento(
                request.possuiPrelancamento()
        );

        livro.setDataLancamento(
                request.dataLancamento()
        );

        return livro;
    }

    public LivroResponse toResponse(Livro livro) {

        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),

                autorMapper.toResponse(
                        livro.getAutor()
                ),

                categoriaMapper.toResponse(
                        livro.getCategoria()
                ),

                editoraMapper.toResponse(
                        livro.getEditora()
                ),

                //!Tipo
                livro.getTipo(),

                //!Físico
                livro.getQuantidadeDisponivel(),

                //!Extra
                livro.getLimiteReservas(),

                //!Dados gerais
                livro.getNumeroDePaginas(),
                livro.getAno(),

                //!Virtual
                livro.getPermiteDownload(),
                livro.getTamanhoMaximoArquivo(),
                livro.getPossuiExpiracao(),
                livro.getDiasExpiracao(),
                livro.getQuantidadeAcessosDisponiveis(),

                //!Lançamento
                livro.getPossuiPrelancamento(),
                livro.getDataLancamento()
        );
    }
}