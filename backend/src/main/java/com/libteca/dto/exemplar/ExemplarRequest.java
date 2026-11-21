package com.libteca.dto.exemplar;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.libteca.enums.TypeLivro;
public record ExemplarRequest(
        @NotNull @Positive Long livroId,
        @NotNull TypeLivro tipo,
        @NotNull LocalDate dataAquisicao,
        @Size(max = 255) String caminhoArquivo,
        @Size(max = 60) String codigoPatrimonio
) {}
