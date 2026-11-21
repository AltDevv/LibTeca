package com.libteca.config;

import com.libteca.controller.BancoInicialController;
import com.libteca.service.BancoInicialService;
import com.libteca.handler.ErroInesperadoException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("instalacao-banco")
@EnableAutoConfiguration(excludeName = {
    "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
    "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
    "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
    "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration"
})
@Import({BancoInicialController.class, BancoInicialService.class, InstalacaoConfig.class, ErroInesperadoException.class})
public class BancoInicialConfig {
    @Bean
    ApplicationRunner codigoBanco(InstalacaoConfig codigo) { return args -> codigo.gerarCodigo(); }

    @Bean
    SecurityFilterChain segurancaBanco(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/css/**", "/assets/**", "/controller/**", "/config/**",
                        "/dto/**", "/entity/**", "/enums/**", "/filter/**", "/handler/**", "/mapper/**",
                        "/repository/**", "/service/**", "/specification/**", "/telas/**", "/error",
                        "/api/biblioteca", "/api/auth/csrf", "/api/instalacao/banco").permitAll()
                .anyRequest().denyAll())
                .exceptionHandling(e -> e.authenticationEntryPoint((req,res,erro) -> res.setStatus(401)))
                .build(); // CSRF e código local também são exigidos na seleção do banco.
    }
}
