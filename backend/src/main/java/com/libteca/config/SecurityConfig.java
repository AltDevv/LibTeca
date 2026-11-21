package com.libteca.config;

import com.libteca.repository.*;
import com.libteca.service.AcessoService;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarios, FuncionarioRepository funcionarios) {
        return email -> {
            var usuario = usuarios.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
            boolean ativo = funcionarios.findByUsuarioId(usuario.getId()).map(f -> Boolean.TRUE.equals(f.getAtivo())).orElse(true);
            return User.withUsername(usuario.getEmail()).password(usuario.getSenha())
                    .roles(usuario.getRole().name()).disabled(!ativo).build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AcessoService acesso) throws Exception {
        http.cors(cors -> {}).authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/css/**", "/assets/**", "/config/**", "/controller/**",
                        "/dto/**", "/entity/**", "/enums/**", "/filter/**", "/handler/**", "/mapper/**",
                        "/repository/**", "/service/**", "/specification/**", "/telas/**", "/error").permitAll()
                .requestMatchers("/api/auth/csrf", "/api/auth/entrar", "/api/auth/cadastro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/biblioteca").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/instalacao").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/biblioteca").access((a,c) -> new AuthorizationDecision(acesso.administrador(a.get())))
                .requestMatchers("/api/biblioteca/transferir").access((a,c) -> new AuthorizationDecision(acesso.administrador(a.get())))
                .requestMatchers(HttpMethod.DELETE, "/api/biblioteca").access((a,c) -> new AuthorizationDecision(acesso.administrador(a.get())))
                .requestMatchers(HttpMethod.GET, "/api/livros/**", "/api/autores", "/api/categorias", "/api/editoras",
                        "/api/catalogo/**", "/capas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/funcionarios/**").access((a,c) -> new AuthorizationDecision(acesso.gestao(a.get())))
                .requestMatchers("/api/funcionarios/**").access((a,c) -> new AuthorizationDecision(acesso.administrador(a.get())))
                .requestMatchers("/api/solicitacoes-funcionarios/**").denyAll()
                .requestMatchers("/api/auth/**", "/api/minha-conta/**", "/api/arquivos/leitura/**").authenticated()
                .requestMatchers("/api/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .access((a,c) -> new AuthorizationDecision(acesso.gestao(a.get())))
                .anyRequest().denyAll())
            .formLogin(form -> form.loginProcessingUrl("/api/auth/entrar").usernameParameter("email")
                    .passwordParameter("senha")
                    .successHandler((req,res,auth) -> res.setStatus(204))
                    .failureHandler((req,res,ex) -> { res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                        res.getWriter().write("{\"mensagem\":\"Email ou senha inválidos, ou conta inativa\"}"); }))
            .logout(logout -> logout.logoutUrl("/api/auth/sair").logoutSuccessHandler((req,res,auth) -> res.setStatus(204)))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req,res,e) -> { res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); res.getWriter().write("{\"mensagem\":\"Entre na sua conta para continuar\"}"); })
                    .accessDeniedHandler((req,res,e) -> { res.setStatus(403); res.setContentType("application/json;charset=UTF-8"); res.getWriter().write("{\"mensagem\":\"Acesso não permitido ou sessão expirada. Atualize a página\"}"); }))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        // CSRF permanece ativo. O frontend obtém o token em /api/auth/csrf.
        return http.build();
    }
}
