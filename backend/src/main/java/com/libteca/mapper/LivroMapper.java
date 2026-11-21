package com.libteca.mapper;

import com.libteca.dto.livro.LivroRequest;
import com.libteca.dto.livro.LivroResponse;
import com.libteca.entity.*;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Comparator;

@Component
public class LivroMapper {

    private final AutorMapper autorMapper;
    private final CategoriaMapper categoriaMapper;
    private final EditoraMapper editoraMapper;
    private final com.libteca.repository.ReservaRepository reservas;

    public LivroMapper(AutorMapper autorMapper, CategoriaMapper categoriaMapper, EditoraMapper editoraMapper,
                       com.libteca.repository.ReservaRepository reservas) {
        this.reservas = reservas;
        this.autorMapper = autorMapper;
        this.categoriaMapper = categoriaMapper;
        this.editoraMapper = editoraMapper;
    }

    public Livro toEntity(LivroRequest request, Set<Autor> autores, Set<Categoria> categorias,
                          Editora editora, Funcionario funcionario) {
        Livro livro = new Livro();
        livro.setTitulo(request.titulo());
        livro.setAutores(autores);
        livro.setCategorias(categorias);
        livro.setEditora(editora);
        livro.setFuncionario(funcionario);
        livro.setCapa(request.capa());
        livro.setNumeroDePaginas(request.numeroDePaginas());
        livro.setDataLancamento(request.dataLancamento());
        if (request.limiteReservas() != null) livro.setLimiteReservas(request.limiteReservas());
        return livro;
    }

    public LivroResponse toResponse(Livro livro) {
        return new LivroResponse(
                livro.getId(), livro.getTitulo(),
                livro.getAutores().stream().sorted(Comparator.comparing(Autor::getId)).map(autorMapper::toResponse).toList(),
                livro.getCategorias().stream().sorted(Comparator.comparing(Categoria::getId)).map(categoriaMapper::toResponse).toList(),
                editoraMapper.toResponse(livro.getEditora()), livro.getFuncionario().getId(),
                livro.getCapa(), livro.getNumeroDePaginas(), livro.getDataLancamento(), livro.getLimiteReservas(),
                reservas.countByExemplarLivroIdAndStatusIn(livro.getId(), com.libteca.service.reserva.ReservaLancamentoService.EM_ANDAMENTO));
    }
}
