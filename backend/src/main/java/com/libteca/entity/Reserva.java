package com.libteca.entity;

import com.libteca.enums.StatusReserva;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Reserva{

    //Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //livro
    @NotNull(message = "O livro é obrigatório")
    @ManyToOne
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    //usuário que reservou o livro
    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    //!Deve ser gerado pelo backend, não deve ser parte da entidade
    //data em que a reserva foi feita
    @NotNull(message = "A data da reserva é obrigatória")
    @Column(nullable = false)
    private LocalDate dataReserva;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status = StatusReserva.ATIVA;//status é o nome da variável
}
