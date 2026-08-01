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

    //data em que a reserva foi feita
    @NotNull(message = "A data da reserva é obrigatória")
    @Column(nullable = false)
    private LocalDate dataReserva;

    //se a reserva ainda está ativa (false = cancelada ou concluída)
    @Column(nullable = false)
    private Boolean ativa = true;

}
