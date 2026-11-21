package com.libteca.config;

import com.libteca.service.BibliotecaService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class InstalacaoInicialConfig {
    @Bean
    public ApplicationRunner prepararInstalacao(BibliotecaService biblioteca, InstalacaoConfig instalacao,
            com.libteca.service.ConfiguracaoLocalService configuracaoLocal,
            @Value("${libteca.instalacao.gerar-codigo:true}") boolean gerar) {
        return args -> {
            configuracaoLocal.preparar();
            var info=biblioteca.informacoes();
            if(gerar && !info.configurada() && !info.possuiAdministrador()) instalacao.gerarCodigo();
        };
    }
}
