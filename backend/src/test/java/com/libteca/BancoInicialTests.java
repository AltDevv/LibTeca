package com.libteca;

import com.libteca.config.BancoInicialConfig;
import com.libteca.dto.biblioteca.BancoRequest;
import com.libteca.service.BancoInicialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=BancoInicialConfig.class)
@ActiveProfiles("instalacao-banco")
class BancoInicialTests {
    @Autowired WebApplicationContext contexto;
    @DynamicPropertySource
    static void pasta(DynamicPropertyRegistry r) throws Exception {
        Path dir=Files.createTempDirectory("libteca-instalacao-teste-");
        r.add("libteca.instalacao.diretorio",()->dir.toString());
    }
    @Test
    void abreAssistenteSemBancoEExigeCodigoECsrf() throws Exception {
        assertTrue(contexto.getBeansOfType(javax.sql.DataSource.class).isEmpty());
        var mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        mvc.perform(get("/api/biblioteca")).andExpect(status().isOk()).andExpect(jsonPath("$.escolherBanco").value(true));
        mvc.perform(post("/api/instalacao/banco").contentType("application/json").content("{\"tipo\":\"sqlite\",\"codigo\":\"errado\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/instalacao/banco").with(csrf()).contentType("application/json").content("{\"tipo\":\"sqlite\",\"codigo\":\"errado\"}"))
                .andExpect(status().isForbidden());
    }
    @Test
    void configuracaoNaoPermiteInjetarOpcoesNaConexao() {
        var sqlite=BancoInicialService.propriedades(new BancoRequest("sqlite","codigo",null,null,null,null,null),Path.of("data"));
        assertTrue(sqlite.getProperty("spring.datasource.url").endsWith("libteca.db"));
        assertNull(sqlite.getProperty("spring.datasource.password"));
        assertThrows(IllegalArgumentException.class,()->BancoInicialService.propriedades(
                new BancoRequest("mysql","codigo","localhost?x",3306,"libteca","root","senha"),Path.of("data")));
    }
}
