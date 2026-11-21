package com.libteca.repository;

import org.springframework.data.repository.query.Param;

import com.libteca.entity.Reserva;
import com.libteca.enums.StatusReserva;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByExemplarId(Long exemplarId);
    long countByExemplarLivroIdAndStatusIn(Long livroId, List<StatusReserva> status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reserva> findByExemplarLivroIdAndStatusIn(Long livroId, List<StatusReserva> status);

    java.util.List<Reserva> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reserva> findByExemplarIdAndStatusInOrderByDataReservaAscIdAsc(Long exemplarId, List<StatusReserva> status);

    List<Reserva> findByStatusAndExemplarLivroDataLancamentoLessThanEqual(StatusReserva status, LocalDate data);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> buscarParaAtualizar(@Param("id") Long id);
}
