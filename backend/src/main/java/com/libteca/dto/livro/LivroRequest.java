package com.libteca.dto.livro;

import com.libteca.enums.TypeLivro;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LivroRequest(

        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotNull(message = "O autor é obrigatório")
        Long autorId,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "A editora é obrigatória")
        Long editoraId,

        //!Físico
        @Min(0)
        Integer quantidadeDisponivel,

        @NotNull
        @Min(1)
        Integer numeroDePaginas,

        @NotNull
        @Min(1)
        Integer ano,

        //!Extra
        @Min(value = 0, message = "O limite de reservas não pode ser negativo")
        @Column
        Integer limiteReservas,

        //!Tipo
        @NotNull(message = "O tipo do livro é obrigatório")
        TypeLivro tipo,

        //!Virtual
        Boolean permiteDownload,

        @Min(1)
        Long tamanhoMaximoArquivo,

        Boolean possuiExpiracao,

        @Min(1)
        Integer diasExpiracao,

        Integer quantidadeAcessosDisponiveis,

        //!Lançamento
        Boolean possuiPrelancamento,

        LocalDate dataLancamento

) {}