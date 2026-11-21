package com.libteca.controller;

import com.libteca.dto.telefone.*;
import com.libteca.service.TelefoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/telefones")
public class TelefoneController {

    private final TelefoneService telefoneService;

    public TelefoneController(TelefoneService telefoneService) {
        this.telefoneService = telefoneService;
    }

    @GetMapping
    public List<TelefoneResponse> listarTodos() {
        return telefoneService.listarTodos();
    }

    @GetMapping("/{id}")
    public TelefoneResponse mostrarTelefone(@PathVariable Long id) {
        return telefoneService.mostrarTelefone(id);
    }

    @PostMapping
    public TelefoneResponse adicionarTelefone(@Valid @RequestBody TelefoneRequest request) {
        return telefoneService.adicionarTelefone(request);
    }

    @DeleteMapping("/{id}")
    public void apagarTelefone(@PathVariable Long id) {
        telefoneService.apagarTelefone(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        telefoneService.apagarTodos();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<TelefoneResponse> buscarPorUsuario(@PathVariable Long usuarioId) {
        return telefoneService.buscarPorUsuario(usuarioId);
    }
}
