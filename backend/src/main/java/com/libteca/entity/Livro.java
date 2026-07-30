package com.libteca.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Entity
public class Livro{

    //Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //título
    @NotBlank(message = "O título é obrigatório")
    @Column(nullable = false)
    private String titulo;

    //autor
    @NotBlank
    @Column(nullable = false)
    private String autor;

    //categoria
    @NotBlank
    @Column(nullable = false)
    private String categoria;

    //quantidade de livros
    @Min(0)
    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    //número de páginas
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer numeroDePaginas;

    //Ano de lançamento
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer ano;

}
