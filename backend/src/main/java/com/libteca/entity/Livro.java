package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlivro")
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150)
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @ManyToMany
    @JoinTable(name = "livro_autor",
            joinColumns = @JoinColumn(name = "idlivro"),
            inverseJoinColumns = @JoinColumn(name = "idautor"))
    private Set<Autor> autores = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "livro_categoria",
            joinColumns = @JoinColumn(name = "idlivro"),
            inverseJoinColumns = @JoinColumn(name = "idcategoria"))
    private Set<Categoria> categorias = new LinkedHashSet<>();

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "ideditora", nullable = false)
    private Editora editora;

    // Funcionário responsável pelo cadastro.
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idfuncionario", nullable = false)
    private Funcionario funcionario;

    @Size(max = 255)
    @Column(name = "capa", length = 255)
    private String capa;

    @NotNull
    @Min(1)
    @Column(name = "numero_de_paginas", nullable = false)
    private Integer numeroDePaginas;

    @NotNull
    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    // Zero desativa novas reservas; reservas já feitas são preservadas.
    @NotNull
    @Min(0)
    @Column(name = "limite_reservas", nullable = false)
    private Integer limiteReservas = 3;

}
