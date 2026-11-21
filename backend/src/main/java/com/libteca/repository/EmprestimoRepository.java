package com.libteca.repository;

import org.springframework.data.repository.query.Param;

import com.libteca.entity.Emprestimo;
import com.libteca.enums.StatusEmprestimo;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    boolean existsByExemplarId(Long exemplarId);

    java.util.List<Emprestimo> findByUsuarioIdOrderByIdDesc(Long usuarioId);
    List<Emprestimo> findByStatusAndDataExpiracaoBefore(StatusEmprestimo status, LocalDate data);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Emprestimo> findByExemplarIdAndDataDevolucaoIsNull(Long exemplarId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Emprestimo e where e.id = :id")
    Optional<Emprestimo> buscarParaAtualizar(@Param("id") Long id);
}
