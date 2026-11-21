package com.libteca.filter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import com.libteca.enums.TypeLivro;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class LivroFiltro {

    private String titulo;
    private String autor;
    private String categoria;
    private String editora;
    private TypeLivro tipoLivro;
    private LocalDate dataLancamento;
    private Boolean possuiPrelancamento;
    @Min(0)
    private Integer quantidadeDisponivel;
    @Min(0)
    private Integer quantidadeMinima;
    @Min(0)
    private Integer quantidadeMaxima;
    @Min(1)
    private Integer numeroDePaginas;
    @Min(1)
    private Integer paginaMinima;
    @Min(1)
    private Integer paginaMaxima;
    @Min(1) @Max(9999)
    private Integer ano;
    @Min(1) @Max(9999)
    private Integer anoMinimo;
    @Min(1) @Max(9999)
    private Integer anoMaximo;
}
