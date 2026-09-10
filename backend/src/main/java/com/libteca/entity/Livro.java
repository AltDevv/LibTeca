package com.libteca.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

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
    @NotNull(message = "O autor é obrigatório")
    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Autor autor;

    //categoria
    @NotNull(message = "A categoria é obrigatória")
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    //editora
    @NotNull(message = "A editora é obrigatória")
    @ManyToOne
    @JoinColumn(name = "editora_id", nullable = false)
    private Editora editora;

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
