package com.libteca.controller;

import com.libteca.service.BibliotecaService;
import com.libteca.dto.biblioteca.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class BibliotecaController {
    private final BibliotecaService biblioteca;
    public BibliotecaController(BibliotecaService biblioteca) { this.biblioteca=biblioteca; }
    @GetMapping("/api/biblioteca")
    public BibliotecaResponse informacoes(){return biblioteca.informacoes();}
    @PostMapping("/api/instalacao")
    public BibliotecaResponse instalar(@Valid @RequestBody InstalacaoRequest request){return biblioteca.instalar(request);}
    @PutMapping("/api/biblioteca")
    public BibliotecaResponse atualizar(@Valid @RequestBody BibliotecaRequest request){return biblioteca.atualizar(request);}
}
