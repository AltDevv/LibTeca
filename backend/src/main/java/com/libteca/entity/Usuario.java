package com.libteca.entity;

import com.libteca.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Getter
@Setter
@Entity
public class Usuario{

    //Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //nome
    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    //email
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    //senha
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Column(nullable = false)
    private String senha;

    //Tipo de usuário(role)
    @NotNull(message = "A role é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

}
