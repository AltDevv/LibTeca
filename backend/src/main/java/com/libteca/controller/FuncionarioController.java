package com.libteca.controller;

import com.libteca.dto.funcionario.*;
import com.libteca.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public List<FuncionarioResponse> listarTodos() {
        return funcionarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public FuncionarioResponse mostrarFuncionario(@PathVariable Long id) {
        return funcionarioService.mostrarFuncionario(id);
    }

    @PostMapping
    public FuncionarioResponse adicionarFuncionario(@Valid @RequestBody FuncionarioRequest request) {
        return funcionarioService.adicionarFuncionario(request);
    }

    @DeleteMapping("/{id}")
    public void apagarFuncionario(@PathVariable Long id) {
        funcionarioService.apagarFuncionario(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        funcionarioService.apagarTodos();
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizarFuncionario(@PathVariable Long id, @Valid @RequestBody FuncionarioRequest request) {
        return funcionarioService.atualizarFuncionario(id, request);
    }
}
