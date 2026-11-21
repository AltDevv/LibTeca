package com.libteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "libteca.arquivos")
@Getter
@Setter
public class ArquivoConfig {
    private String diretorio = "./data/arquivos";
    private long tamanhoMaximo = 20971520;
}
