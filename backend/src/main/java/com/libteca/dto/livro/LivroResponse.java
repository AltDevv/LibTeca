package com.libteca.dto.livro;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import com.libteca.dto.autor.AutorResponse;
import com.libteca.dto.categoria.CategoriaResponse;
import com.libteca.dto.editora.EditoraResponse;
public record LivroResponse(
        Long id,
        String titulo,
        List<AutorResponse> autores,
        List<CategoriaResponse> categorias,
        EditoraResponse editora,
        Long funcionarioId,
        String capa,
        Integer numeroDePaginas,
        LocalDate dataLancamento,
        Integer limiteReservas,
        long reservasEmAndamento
) {}
