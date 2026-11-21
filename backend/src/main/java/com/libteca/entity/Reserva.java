package com.libteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.libteca.enums.StatusReserva;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idreserva")
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
    @Column(name = "data_reserva", nullable = false)
    private LocalDate dataReserva;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status_reserva", nullable = false, length = 25)
    private StatusReserva status = StatusReserva.ATIVA;

}
