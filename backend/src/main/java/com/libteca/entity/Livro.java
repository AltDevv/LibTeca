package com.libteca.entity;

import com.libteca.enums.TypeLivro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Livro{

    //!Atributos básicos
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

    //!Atributos extras
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

    //Físico ou tipoEmprestimo
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeLivro tipo;

    //!Atributos do Livro Virtual
    private Boolean permiteDownload;

    @Min(1)
    private Long tamanhoMaximoArquivo;

    private Boolean possuiExpiracao;

    @Min(1)
    private Integer diasExpiracao;

    //Quantidade de possibilidade de acesso a um livro
    //Usado para livros digitais, sendo o Null representando o infinito
    private Integer quantidadeAcessosDisponiveis;

    private Boolean possuiPrelancamento;

    private LocalDate dataLancamento;

}
