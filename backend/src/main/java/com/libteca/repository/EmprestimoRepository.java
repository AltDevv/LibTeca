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
    long countByExemplarIdAndStatusNot(Long id, StatusEmprestimo status);
    long countByExemplarIdAndStatusAndDataDevolucaoIsNullAndDataExpiracaoGreaterThanEqual(Long id, StatusEmprestimo status, LocalDate hoje);

    java.util.List<Emprestimo> findByUsuarioIdOrderByIdDesc(Long usuarioId);
    List<Emprestimo> findByStatusAndDataExpiracaoBefore(StatusEmprestimo status, LocalDate data);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Emprestimo e where e.exemplar.id = :exemplarId and e.dataDevolucao is null and e.status <> com.libteca.enums.StatusEmprestimo.CANCELADO")
    List<Emprestimo> buscarEmAndamentoPorExemplar(@Param("exemplarId") Long exemplarId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Emprestimo e where e.id = :id")
    Optional<Emprestimo> buscarParaAtualizar(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("update Emprestimo e set e.status = com.libteca.enums.StatusEmprestimo.ATIVO, e.dataEmprestimo = :hoje, e.dataExpiracao = :prazo where e.id = :id and e.status = com.libteca.enums.StatusEmprestimo.PENDENTE and e.dataDevolucao is null")
    int confirmarRecebimento(@Param("id") Long id, @Param("hoje") LocalDate hoje, @Param("prazo") LocalDate prazo);

    @Modifying(flushAutomatically = true)
    @Query("update Emprestimo e set e.status = com.libteca.enums.StatusEmprestimo.CANCELADO where e.id = :id and e.status = com.libteca.enums.StatusEmprestimo.PENDENTE and e.dataDevolucao is null")
    int cancelarSolicitacao(@Param("id") Long id);
}
