package com.libteca.repository;

import com.libteca.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    boolean existsByUsuarioId(Long usuarioId);
    java.util.Optional<Funcionario> findByUsuarioId(Long usuarioId);
}
