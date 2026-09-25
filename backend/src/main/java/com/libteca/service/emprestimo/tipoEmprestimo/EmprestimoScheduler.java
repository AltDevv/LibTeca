package com.libteca.service.emprestimo.tipoEmprestimo;

import com.libteca.service.emprestimo.EmprestimoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
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