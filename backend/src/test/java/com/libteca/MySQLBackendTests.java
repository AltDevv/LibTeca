package com.libteca;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Usar somente um banco vazio de testes: schema-modelo.sql cria as tabelas.
@SpringBootTest
@ActiveProfiles("mysql-test")
@EnabledIfEnvironmentVariable(named = "LIBTECA_MYSQL_TEST_URL", matches = ".+")
class MySQLBackendTests extends FluxosBancoTests {
}
