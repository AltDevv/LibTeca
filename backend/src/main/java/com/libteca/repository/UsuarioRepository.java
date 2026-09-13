package com.libteca.repository;

import com.libteca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    //Verificação não contando com o própio ID
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmail(String email);
}

