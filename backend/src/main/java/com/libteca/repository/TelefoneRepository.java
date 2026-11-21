package com.libteca.repository;

import com.libteca.entity.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
    boolean existsByNumero(String numero);
    List<Telefone> findByUsuarioId(Long usuarioId);
}
