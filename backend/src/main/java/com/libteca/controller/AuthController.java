package com.libteca.controller;

import com.libteca.dto.usuario.*;
import com.libteca.mapper.UsuarioMapper;
import com.libteca.repository.FuncionarioRepository;
import com.libteca.service.*;
import jakarta.validation.Valid;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AcessoService acesso;
    private final UsuarioService usuarios;
    private final UsuarioMapper mapper;
    private final FuncionarioRepository funcionarios;
    private final BibliotecaService biblioteca;
    public AuthController(AcessoService acesso, UsuarioService usuarios, UsuarioMapper mapper, FuncionarioRepository funcionarios, BibliotecaService biblioteca) {
        this.acesso = acesso; this.usuarios = usuarios; this.mapper = mapper; this.funcionarios = funcionarios;
        this.biblioteca = biblioteca;
    }
    @GetMapping("/csrf")
    public Map<String,String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
    @GetMapping("/eu")
    public Map<String,Object> eu() {
        var usuario = acesso.atual();
        return Map.of("usuario", mapper.toResponse(usuario), "funcionarioId",
                funcionarios.findByUsuarioId(usuario.getId()).map(f -> f.getId()).orElse(0L));
    }
    @PostMapping("/cadastro")
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        biblioteca.exigirConfigurada();
        return usuarios.cadastrarUsuario(request);
    }
}
