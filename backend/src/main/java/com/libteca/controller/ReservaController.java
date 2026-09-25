package com.libteca.controller;

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

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
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
        return reservaService.adicionarReserva(request);
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelarReserva(@PathVariable Long id) {
        return reservaService.cancelarReserva(id);
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
