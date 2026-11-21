package com.libteca.controller;

import com.libteca.dto.exemplar.*;
import com.libteca.service.ExemplarService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exemplares")
public class ExemplarController {

    private final ExemplarService exemplarService;

    public ExemplarController(ExemplarService exemplarService) {
        this.exemplarService = exemplarService;
    }

    @GetMapping
    public List<ExemplarResponse> listarTodos() {
        return exemplarService.listarTodos();
    }

    @GetMapping("/{id}")
    public ExemplarResponse mostrarExemplar(@PathVariable Long id) {
        return exemplarService.mostrarExemplar(id);
    }

    @PostMapping
    public ExemplarResponse adicionarExemplar(@Valid @RequestBody ExemplarRequest request) {
        return exemplarService.adicionarExemplar(request);
    }

    @PutMapping("/{id}")
    public ExemplarResponse atualizarExemplar(@PathVariable Long id, @Valid @RequestBody ExemplarRequest request) {
        return exemplarService.atualizarExemplar(id, request);
    }

    @PostMapping("/virtuais")
    public List<ExemplarResponse> adicionarVirtuais(@Valid @RequestBody ExemplarRequest request) {
        return exemplarService.adicionarVirtuais(request);
    }

    @DeleteMapping("/{id}")
    public void apagarExemplar(@PathVariable Long id) {
        exemplarService.apagarExemplar(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        exemplarService.apagarTodos();
    }

    @GetMapping("/livro/{livroId}")
    public List<ExemplarResponse> buscarPorLivro(@PathVariable Long livroId) {
        return exemplarService.buscarPorLivro(livroId);
    }
}
