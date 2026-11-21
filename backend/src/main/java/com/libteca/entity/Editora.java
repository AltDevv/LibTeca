package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "editora")
public class Editora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ideditora")
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 45)
    @Column(name = "nome_editora", nullable = false, length = 45)
    private String nome;

}
