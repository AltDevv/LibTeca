package com.libteca.repository;

import com.libteca.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LivroRepository
        extends JpaRepository<Livro, Long>,
        JpaSpecificationExecutor<Livro> {
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Livro l set l.limiteReservas = l.limiteReservas where l.id = :livroId")
    int bloquearReservas(@org.springframework.data.repository.query.Param("livroId") Long livroId);
    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Livro l set l.limiteReservas=l.limiteReservas where l.id=:id")
    int bloquearCadastro(@org.springframework.data.repository.query.Param("id") Long id);
}
