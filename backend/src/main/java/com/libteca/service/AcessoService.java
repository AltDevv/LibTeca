package com.libteca.service;

import com.libteca.entity.Usuario;
import com.libteca.enums.Role;
import com.libteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AcessoService {
    private final UsuarioRepository usuarios;
    private final FuncionarioRepository funcionarios;
    public AcessoService(UsuarioRepository usuarios, FuncionarioRepository funcionarios) {
        this.usuarios = usuarios; this.funcionarios = funcionarios;
    }
    public Usuario atual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return usuarios.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    public boolean administrador(Authentication auth) {
        if (auth == null) return false;
        return usuarios.findByEmail(auth.getName()).map(u -> (u.getRole() == Role.ADMIN || u.getRole() == Role.ADMIM)
                && funcionarios.findByUsuarioId(u.getId()).map(f -> Boolean.TRUE.equals(f.getAtivo())).orElse(true)).orElse(false);
    }
    public boolean gestao(Authentication auth) {
        if (administrador(auth)) return true;
        if (auth == null) return false;
        return usuarios.findByEmail(auth.getName()).map(u -> u.getRole() == Role.FUNCIONARIO
                && funcionarios.findByUsuarioId(u.getId()).map(f -> Boolean.TRUE.equals(f.getAtivo())).orElse(false)).orElse(false);
    }
    public Long funcionarioAtual() {
        return funcionarios.findByUsuarioId(atual().getId()).filter(f -> Boolean.TRUE.equals(f.getAtivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "É necessário um cadastro ativo de funcionário")).getId();
    }
    public void dono(Long usuarioId) {
        if (!atual().getId().equals(usuarioId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este registro pertence a outra conta");
    }
}
