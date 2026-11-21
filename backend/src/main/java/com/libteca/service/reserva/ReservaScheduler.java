package com.libteca.service.reserva;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "libteca.agendamentos", havingValue = "true", matchIfMissing = true)
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
