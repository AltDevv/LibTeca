package com.libteca.controller;

import com.libteca.dto.emprestimo.EmprestimoRequest;
import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.service.emprestimo.EmprestimoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


import java.util.List;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @GetMapping
    public Page<EmprestimoResponse> listarTodos(Pageable pageable) {
        return emprestimoService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    public EmprestimoResponse mostrarEmprestimo(@PathVariable Long id) {
        return emprestimoService.mostrarEmprestimo(id);
    }

    @PostMapping
    public EmprestimoResponse adicionarEmprestimo(@Valid @RequestBody EmprestimoRequest request) {
        return emprestimoService.adicionarEmprestimo(request);
    }

    @PatchMapping("/{id}/devolver")
    public EmprestimoResponse devolverEmprestimo(@PathVariable Long id) {
        return emprestimoService.devolverEmprestimo(id);
    }

    @DeleteMapping("/{id}")
    public void apagarEmprestimo(@PathVariable Long id) {
        emprestimoService.apagarEmprestimo(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        emprestimoService.apagarTodos();
    }
}
