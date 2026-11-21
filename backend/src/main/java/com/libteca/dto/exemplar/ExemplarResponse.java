package com.libteca.dto.exemplar;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.libteca.enums.TypeLivro;
import com.libteca.enums.StatusExemplar;
import com.libteca.dto.livro.LivroResponse;
public record ExemplarResponse(
        Long id,
        LivroResponse livro,
        StatusExemplar status,
        TypeLivro tipo,
        LocalDate dataAquisicao,
        String caminhoArquivo,
        String codigoPatrimonio,
        long emprestimosAtivos,
        long totalEmprestimos
) {}
