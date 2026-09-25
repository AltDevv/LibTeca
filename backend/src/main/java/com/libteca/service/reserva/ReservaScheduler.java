package com.libteca.service.reserva;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaScheduler {

    private final ReservaLancamentoService reservaLancamentoService;

    public ReservaScheduler(
            ReservaLancamentoService reservaLancamentoService
    ) {
        this.reservaLancamentoService = reservaLancamentoService;
    }

    @Scheduled(fixedRate = 60000)
    public void verificarLancamentos() {
        reservaLancamentoService.processarReservasDeLancamento();
    }
}
