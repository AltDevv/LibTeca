package com.libteca.config;

import org.springframework.context.annotation.*;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.DeserializationFeature;

@Configuration
public class JSONConfig {
    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        // Rejeita atributos desconhecidos para não ignorar erros de escrita nas requisições.
        return builder -> builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
