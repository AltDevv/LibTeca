package com.libteca.repository;

import com.libteca.entity.Biblioteca;
import org.springframework.data.jpa.repository.*;

public interface BibliotecaRepository extends JpaRepository<Biblioteca, Long> {
    @Modifying
    @Query("update Biblioteca b set b.configurada = b.configurada where b.id = 1")
    int bloquear();
    @Modifying
    @Query("update Biblioteca b set b.acervoIniciado = 1 where b.id = 1")
    void iniciarAcervo();
}
