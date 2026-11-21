package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcategoria")
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 45)
    @Column(name = "tipo_categoria", nullable = false, length = 45)
    private String nome;

}
