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
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Emprestimo{

    //Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //livro
    @NotNull(message = "O livro é obrigatório")
    @ManyToOne
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    //usuário que pegou o livro emprestado
    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    //data em que o empréstimo foi feito
    @NotNull(message = "A data do empréstimo é obrigatória")
    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    // Data em que o acesso ao livro virtual expira.
    // Nulo quando não houver expiração.
    private LocalDate dataExpiracao;

    //data em que o livro foi de fato devolvido (nulo enquanto não devolvido)
    //serve para representar se ele foi devolvido ou não verificando se é nulo.
    @Column(nullable = true)
    private LocalDate dataDevolucao;

}
