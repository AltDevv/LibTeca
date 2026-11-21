package com.libteca.controller;

import com.libteca.dto.biblioteca.BancoRequest;
import com.libteca.service.BancoInicialService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@Profile("instalacao-banco")
public class BancoInicialController {
    private final BancoInicialService banco;
    public BancoInicialController(BancoInicialService banco) { this.banco=banco; }
    @GetMapping("/api/biblioteca")
    public Map<String,Object> informacoes() {
        return Map.of("nome","LibTeca","configurada",false,"possuiAdministrador",false,
                "escolherBanco",true,"erroBanco",BancoInicialService.erroInicializacao());
    }
    @GetMapping("/api/auth/csrf")
    public Map<String,String> csrf(CsrfToken token) {
        return Map.of("token",token.getToken(),"headerName",token.getHeaderName());
    }
    @PostMapping("/api/instalacao/banco")
    public Map<String,String> configurar(@Valid @RequestBody BancoRequest request) throws java.io.IOException {
        banco.configurar(request);
        return Map.of("mensagem","Banco configurado. Iniciando a biblioteca…");
    }
}
