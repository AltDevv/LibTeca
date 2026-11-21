package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.libteca.enums.StatusEmprestimo;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idemprestimo")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idexemplar", nullable = false)
    private Exemplar exemplar;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

    // Nulo quando a operação é feita pelo próprio usuário.
    @ManyToOne
    @JoinColumn(name = "idfuncionario")
    private Funcionario funcionario;

    @NotNull
    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @NotNull
    @Column(name = "data_expiracao", nullable = false)
    private LocalDate dataExpiracao;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "prazo_escolhido", length = 12)
    private com.libteca.enums.PrazoEmprestimo prazo;

    // Data efetiva; permanece nula enquanto o exemplar não for devolvido.
    @Column(name = "data_devolucao")
    private LocalDate dataDevolucao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status_emprestimo", nullable = false, length = 9)
    private StatusEmprestimo status = StatusEmprestimo.ATIVO;

}
