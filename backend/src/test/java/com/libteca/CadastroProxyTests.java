package com.libteca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"sqlite-test", "web"})
class CadastroProxyTests {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registry) throws Exception {
        Path arquivo=Files.createTempFile("libteca-proxy-", ".db");arquivo.toFile().deleteOnExit();
        registry.add("spring.datasource.url",()->"jdbc:sqlite:"+arquivo.toAbsolutePath());
        Path pasta=Files.createTempDirectory("libteca-proxy-config-");
        registry.add("libteca.instalacao.diretorio",()->pasta.toString());
        registry.add("logging.file.name",()->"./logs/proxy-testes.log");
    }
    @Value("${local.server.port}") int porta;
    @Autowired JdbcTemplate jdbc;
    @Autowired com.libteca.repository.BibliotecaRepository configuracao;
    @Autowired com.libteca.config.AdministracaoConfig.Sessoes sessoes;
    private final HttpClient cliente=HttpClient.newHttpClient();
    private final tools.jackson.databind.ObjectMapper json=new tools.jackson.databind.ObjectMapper();
    private static final String PUBLICO="https://biblioteca.ngrok-free.app";

    HttpRequest.Builder requisicao(String caminho, boolean proxy) {
        var builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+porta+caminho));
        if(proxy) builder.header("X-Forwarded-Proto","https").header("X-Forwarded-Host","biblioteca.ngrok-free.app");
        return builder;
    }
    HttpResponse<String> enviar(HttpRequest.Builder builder) throws Exception {
        return cliente.send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }
    String cookie(HttpResponse<String> resposta) {
        return resposta.headers().allValues("set-cookie").stream().filter(c->c.startsWith("JSESSIONID="))
                .findFirst().orElseThrow().split(";",2)[0];
    }
    String cadastro(String email) {
        return "{\"telefone\":\""+String.format("119%08d",Integer.toUnsignedLong(email.hashCode())%100000000)+"\",\"nome\":\"Leitor teste\",\"email\":\""+email+"\",\"senha\":\"SenhaTeste!123\"}";
    }

    @Test void cadastroELoginPorProxyHttpsMantemCsrfECors() throws Exception {
        var dados=configuracao.findById(1L).orElseThrow();dados.setConfigurada(1);configuracao.save(dados);
        var csrf=enviar(requisicao("/api/auth/csrf",true).GET());
        assertEquals(200,csrf.statusCode());
        String sessao=cookie(csrf), token=json.readTree(csrf.body()).get("token").asText();
        assertTrue(csrf.headers().allValues("set-cookie").stream().anyMatch(c->c.contains("Secure")));
        String email=UUID.randomUUID()+"@teste.local";
        var semToken=enviar(requisicao("/api/auth/cadastro",true).header("Origin",PUBLICO).header("Cookie",sessao)
                .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(cadastro(email))));
        assertEquals(403,semToken.statusCode());
        var outraOrigem=enviar(requisicao("/api/auth/cadastro",true).header("Origin","https://outro-site.test").header("Cookie",sessao)
                .header("X-CSRF-TOKEN",token).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(cadastro(email))));
        assertEquals(403,outraOrigem.statusCode());
        assertTrue(outraOrigem.body().contains("Invalid CORS request"));
        var criado=enviar(requisicao("/api/auth/cadastro",true).header("Origin",PUBLICO).header("Cookie",sessao)
                .header("X-CSRF-TOKEN",token).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(cadastro(email))));
        assertEquals(200,criado.statusCode(),criado.body());
        assertEquals("USER",json.readTree(criado.body()).get("role").asText());
        var login=enviar(requisicao("/api/auth/entrar",true).header("Origin",PUBLICO).header("Cookie",sessao)
                .header("X-CSRF-TOKEN",token).header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("email="+email+"&senha=SenhaTeste!123")));
        assertEquals(204,login.statusCode(),login.body());
        var eu=enviar(requisicao("/api/auth/eu",true).header("Cookie",cookie(login)).GET());
        assertEquals(200,eu.statusCode());
        assertEquals(email,json.readTree(eu.body()).get("usuario").get("email").asText());
        sessoes.encerrarTodas();
        assertEquals(401,enviar(requisicao("/api/auth/eu",true).header("Cookie",cookie(login)).GET()).statusCode());
    }

    @Test void cadastroLocalContinuaFuncionando() throws Exception {
        var dados=configuracao.findById(1L).orElseThrow();dados.setConfigurada(1);configuracao.save(dados);
        var csrf=enviar(requisicao("/api/auth/csrf",false).GET());
        var criado=enviar(requisicao("/api/auth/cadastro",false).header("Origin","http://127.0.0.1:"+porta)
                .header("Cookie",cookie(csrf)).header("X-CSRF-TOKEN",json.readTree(csrf.body()).get("token").asText())
                .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(cadastro(UUID.randomUUID()+"@teste.local"))));
        assertEquals(200,criado.statusCode(),criado.body());
    }
}
