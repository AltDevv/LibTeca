package com.libteca.controller;

import com.libteca.dto.biblioteca.*;
import com.libteca.service.AdministracaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biblioteca")
public class AdministracaoController {
    private final AdministracaoService service;
    public AdministracaoController(AdministracaoService service){this.service=service;}
    @PostMapping("/transferir")
    public void transferir(@Valid @RequestBody TransferenciaRequest request){service.transferir(request);}
    @DeleteMapping
    public void apagar(@Valid @RequestBody ExclusaoBibliotecaRequest request) throws java.io.IOException {service.apagar(request);}
}
