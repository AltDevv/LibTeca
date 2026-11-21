package com.libteca.dto.livro;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;
public record LivroRequest(
        @NotBlank(message = "O título é obrigatório") @Size(max = 150) String titulo,
        @NotEmpty(message = "Informe ao menos um autor") Set<@NotNull @Positive Long> autoresIds,
        @NotEmpty(message = "Informe ao menos uma categoria") Set<@NotNull @Positive Long> categoriasIds,
        @NotNull @Positive Long editoraId,
        @Positive Long funcionarioId,
        @Size(max = 255) String capa,
        @NotNull @Min(1) Integer numeroDePaginas,
        @NotNull LocalDate dataLancamento
) {}
