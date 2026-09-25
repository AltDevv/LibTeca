package com.libteca.filter;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LivroFiltro {

        //!Texto
        private String titulo;
        private String autor;
        private String categoria;
        private String editora;

        //!Tipo
        private String tipoLivro;

        //!Virtual
        private Boolean permiteDownload;
        private Boolean acessosIlimitados;

        private Integer quantidadeAcessosDisponiveis;
        private Integer acessosMinimos;
        private Integer acessosMaximos;

        private Boolean possuiExpiracao;
        private Integer diasExpiracao;

        //!Lançamento
        private Boolean possuiPrelancamento;
        private LocalDate dataLancamento;

        //!Físico
        private Integer quantidadeDisponivel;
        private Integer quantidadeMinima;
        private Integer quantidadeMaxima;

        //!Extra
        private Integer limiteReservas;

        //!Páginas
        private Integer numeroDePaginas;
        private Integer paginaMinima;
        private Integer paginaMaxima;

        //!Ano
        private Integer ano;
        private Integer anoMinimo;
        private Integer anoMaximo;

}
