package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "funcionario")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idfuncionario")
    private Long id;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "idusuario", nullable = false, unique = true)
    private Usuario usuario;

    @NotNull
    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @NotNull
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

}
