package com.libteca.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Biblioteca {
    private Long id = 1L;
    private String nome = "Minha biblioteca";
    private String site;
    private Integer configurada = 0;
    private Integer acervoIniciado = 0;
}
