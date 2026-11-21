package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "autor")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idautor")
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 45)
    @Column(name = "nome_autor", nullable = false, length = 45)
    private String nome;

}
