package com.libteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI libtecaOpenApi() {
        return new OpenAPI().info(new Info().title("LibTeca API").version("1.0")
                .description("Catálogo, exemplares, reservas e empréstimos. Use a sessão do site. "
                        + "Nas alterações, envie o token de /api/auth/csrf no cabeçalho X-CSRF-TOKEN."));
    }
}
