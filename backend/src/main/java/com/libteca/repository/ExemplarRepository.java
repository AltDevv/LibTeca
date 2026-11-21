package com.libteca.repository;

import com.libteca.entity.Exemplar;
import com.libteca.enums.StatusExemplar;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    List<Exemplar> findByLivroId(Long livroId);
    long countByLivroId(Long livroId);
    long countByLivroIdAndStatus(Long livroId, StatusExemplar status);
    boolean existsByCodigoPatrimonioAndIdNot(String codigoPatrimonio, Long id);
    boolean existsByCodigoPatrimonio(String codigoPatrimonio);

    // Obtém a escrita da linha dentro da transação, inclusive no SQLite.
    @Modifying(flushAutomatically = true)
    @Query("update Exemplar e set e.status = e.status where e.id = :id")
    int bloquear(@Param("id") Long id);
}
