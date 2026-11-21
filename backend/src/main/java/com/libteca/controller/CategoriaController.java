package com.libteca.controller;

import com.libteca.dto.categoria.CategoriaRequest;
import com.libteca.dto.categoria.CategoriaResponse;
import com.libteca.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listarTodos() {
        return categoriaService.listarTodos();
    }

    @GetMapping("/{id}")
    public CategoriaResponse mostrarCategoria(@PathVariable Long id) {
        return categoriaService.mostrarCategoria(id);
    }

    @PostMapping
    public CategoriaResponse adicionarCategoria(@Valid @RequestBody CategoriaRequest request) {
        return categoriaService.adicionarCategoria(request);
    }

    @DeleteMapping("/{id}")
    public void apagarCategoria(@PathVariable Long id) {
        categoriaService.apagarCategoria(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        categoriaService.apagarTodos();
    }
}
