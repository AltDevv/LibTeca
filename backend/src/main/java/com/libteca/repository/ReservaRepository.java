package com.libteca.repository;

import com.libteca.entity.Reserva;
import com.libteca.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    long countByLivroIdAndStatusIn(
            Long livroId,
            List<StatusReserva> status
    );

    List<Reserva> findByStatusAndLivroPossuiPrelancamentoTrueAndLivroDataLancamentoLessThanEqual(
            StatusReserva status,
            LocalDate data
    );

    boolean existsByLivroIdAndUsuarioIdAndStatusIn(
            Long livroId,
            Long usuarioId,
            List<StatusReserva> status
    );
}