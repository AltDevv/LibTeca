package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "telefone")
public class Telefone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtelefone")
    private Long id;

    @NotBlank(message = "O número é obrigatório")
    @Pattern(regexp = "[0-9]{10,11}", message = "Informe o DDD e o telefone, com 10 ou 11 dígitos")
    @Column(name = "numero", nullable = false, unique = true, length = 11)
    private String numero;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

}
