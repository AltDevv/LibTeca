package com.libteca.config;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import javax.sql.DataSource;
import java.util.*;

@Configuration
@Profile("!instalacao-banco")
public class PrazoSchemaConfig {
    @Bean
    static BeanFactoryPostProcessor prepararSchemaPrazos() {
        return factory -> {
            if (factory.containsBeanDefinition("dataSourceScriptDatabaseInitializer"))
                factory.getBeanDefinition("schemaPrazos").setDependsOn("dataSourceScriptDatabaseInitializer");
            if (factory.containsBeanDefinition("entityManagerFactory")) {
                var definition = factory.getBeanDefinition("entityManagerFactory");
                var dependencias = new ArrayList<String>();
                if (definition.getDependsOn() != null) dependencias.addAll(Arrays.asList(definition.getDependsOn()));
                dependencias.add("schemaPrazos");
                definition.setDependsOn(dependencias.toArray(String[]::new));
            }
        };
    }

    @Bean
    InitializingBean schemaPrazos(DataSource dataSource) {
        return () -> {
            try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
                // Metadados da consulta funcionam tanto no MySQL quanto no SQLite.
                boolean existe = false;
                try (var result = statement.executeQuery("SELECT * FROM emprestimo WHERE 1=0")) {
                    var metadata = result.getMetaData();
                    for (int i = 1; i <= metadata.getColumnCount(); i++)
                        if ("prazo_escolhido".equalsIgnoreCase(metadata.getColumnName(i))) existe = true;
                }
                if (!existe) statement.executeUpdate("ALTER TABLE emprestimo ADD COLUMN prazo_escolhido VARCHAR(12) NULL");
            }
        };
    }
}
