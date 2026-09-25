package com.libteca.repository;

import com.libteca.entity.Emprestimo;
import com.libteca.enums.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findByStatusAndDataExpiracaoLessThanEqual(StatusEmprestimo status, LocalDate date);

}
