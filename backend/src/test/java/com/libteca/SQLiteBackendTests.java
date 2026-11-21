package com.libteca;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@ActiveProfiles("sqlite-test")
class SQLiteBackendTests extends FluxosBancoTests {

    @DynamicPropertySource
    static void configurarBancoTemporario(DynamicPropertyRegistry registry) throws IOException {
        Path banco = Files.createTempFile("libteca-teste-", ".db");
        banco.toFile().deleteOnExit();
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + banco.toAbsolutePath());
    }
}
