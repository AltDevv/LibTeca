package com.libteca.controller;

import com.libteca.dto.emprestimo.EmprestimoResponse;
import com.libteca.dto.reserva.RetiradaReservaRequest;

import com.libteca.dto.reserva.ReservaRequest;
import com.libteca.dto.reserva.ReservaResponse;
import com.libteca.service.reserva.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final com.libteca.service.AcessoService acesso;
    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService, com.libteca.service.AcessoService acesso) {
        this.reservaService = reservaService;
        this.acesso = acesso;
    }

    @GetMapping
    public Page<ReservaResponse> listarTodos(Pageable pageable) {
        return reservaService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    public ReservaResponse mostrarReserva(@PathVariable Long id) {
        return reservaService.mostrarReserva(id);
    }

    @PostMapping
    public ReservaResponse adicionarReserva(@Valid @RequestBody ReservaRequest request) {
        return reservaService.adicionarReserva(new ReservaRequest(request.exemplarId(), request.usuarioId(), acesso.funcionarioAtual()));
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelarReserva(@PathVariable Long id) {
        return reservaService.cancelarReserva(id);
    }

    @PatchMapping("/{id}/retirar")
    public EmprestimoResponse retirarReserva(
            @PathVariable Long id,
            @Valid @RequestBody com.libteca.dto.emprestimo.PrazoRequest request) {
        return reservaService.retirarReserva(id, new RetiradaReservaRequest(request.prazo().calcular(java.time.LocalDate.now()), acesso.funcionarioAtual(), request.prazo()));
    }

    @DeleteMapping("/{id}")
    public void apagarReserva(@PathVariable Long id) {
        reservaService.apagarReserva(id);
    }

    @DeleteMapping
    public void apagarTodos() {
        reservaService.apagarTodos();
    }
}
