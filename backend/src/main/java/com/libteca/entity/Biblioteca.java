package com.libteca.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "configuracao_biblioteca")
@Getter
@Setter
public class Biblioteca {
    @Id
    @Column(name = "id")
    private Long id = 1L;
    @Column(name = "nome", nullable = false, length = 100)
    private String nome = "Minha biblioteca";
    @Column(name = "site", length = 255)
    private String site;
    @Column(name = "configurada", nullable = false)
    private Integer configurada = 0;
    @Column(name = "acervo_iniciado", nullable = false)
    private Integer acervoIniciado = 0;
}
