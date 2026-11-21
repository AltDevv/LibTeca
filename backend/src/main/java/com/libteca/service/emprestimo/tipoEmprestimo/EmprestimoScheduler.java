package com.libteca.service.emprestimo.tipoEmprestimo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.libteca.service.emprestimo.EmprestimoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "libteca.agendamentos", havingValue = "true", matchIfMissing = true)
public class EmprestimoScheduler {

    private final EmprestimoService emprestimoService;

    public EmprestimoScheduler(
            EmprestimoService emprestimoService
    ) {
        this.emprestimoService = emprestimoService;
    }

    @Scheduled(fixedRate = 60000)
    public void verificarExpiracoes() {

        emprestimoService.processarEmprestimosExpirados();
    }
}
