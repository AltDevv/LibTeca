package com.libteca.controller;

import com.libteca.dto.livro.LivroResponse;
import com.libteca.dto.usuario.UsuarioRequest;
import com.libteca.dto.usuario.UsuarioResponse;
import com.libteca.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public Page<UsuarioResponse> listarTodos(Pageable pageable) {
        return usuarioService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    public UsuarioResponse mostrarUsuario(@PathVariable Long id) {
        return usuarioService.mostrarUsuario(id);
    }

    @PostMapping
    public UsuarioResponse adicionarUsuario(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.adicionarUsuario(request);
    }

    @DeleteMapping("/{id}")
    public void apagarUsuario(@PathVariable Long id) {
        usuarioService.apagarUsuario(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        usuarioService.apagarTodos();
    }
}
