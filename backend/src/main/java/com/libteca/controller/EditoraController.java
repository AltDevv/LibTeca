package com.libteca.controller;

import com.libteca.dto.editora.EditoraRequest;
import com.libteca.dto.editora.EditoraResponse;
import com.libteca.service.EditoraService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/editoras")
public class EditoraController {

    private final EditoraService editoraService;

    public EditoraController(EditoraService editoraService) {
        this.editoraService = editoraService;
    }

    @GetMapping
    public List<EditoraResponse> listarTodos() {
        return editoraService.listarTodos();
    }

    @GetMapping("/{id}")
    public EditoraResponse mostrarEditora(@PathVariable Long id) {
        return editoraService.mostrarEditora(id);
    }

    @PostMapping
    public EditoraResponse adicionarEditora(@Valid @RequestBody EditoraRequest request) {
        return editoraService.adicionarEditora(request);
    }

    @DeleteMapping("/{id}")
    public void apagarEditora(@PathVariable Long id) {
        editoraService.apagarEditora(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        editoraService.apagarTodos();
    }
}
