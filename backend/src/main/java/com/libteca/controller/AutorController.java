package com.libteca.controller;

import com.libteca.dto.autor.AutorRequest;
import com.libteca.dto.autor.AutorResponse;
import com.libteca.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @GetMapping
    public List<AutorResponse> listarTodos() {
        return autorService.listarTodos();
    }

    @GetMapping("/{id}")
    public AutorResponse mostrarAutor(@PathVariable Long id) {
        return autorService.mostrarAutor(id);
    }

    @PostMapping
    public AutorResponse adicionarAutor(@Valid @RequestBody AutorRequest request) {
        return autorService.adicionarAutor(request);
    }

    @DeleteMapping("/{id}")
    public void apagarAutor(@PathVariable Long id) {
        autorService.apagarAutor(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        autorService.apagarTodos();
    }
}
