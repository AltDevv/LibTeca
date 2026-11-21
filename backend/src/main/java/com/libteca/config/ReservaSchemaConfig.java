package com.libteca.config;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import javax.sql.DataSource;
import java.util.*;

@Configuration
@Profile("!instalacao-banco")
public class ReservaSchemaConfig {
    @Bean
    static BeanFactoryPostProcessor prepararSchemaReservas() {
        return factory -> {
            if (factory.containsBeanDefinition("dataSourceScriptDatabaseInitializer"))
                factory.getBeanDefinition("schemaReservas").setDependsOn("dataSourceScriptDatabaseInitializer");
            if (factory.containsBeanDefinition("entityManagerFactory")) {
                var definition = factory.getBeanDefinition("entityManagerFactory");
                var dependencias = new ArrayList<String>();
                if (definition.getDependsOn() != null) dependencias.addAll(Arrays.asList(definition.getDependsOn()));
                dependencias.add("schemaReservas");
                definition.setDependsOn(dependencias.toArray(String[]::new));
            }
        };
    }

    @Bean
    InitializingBean schemaReservas(DataSource dataSource) {
        return () -> {
            try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
                // Metadados da consulta funcionam tanto no MySQL quanto no SQLite.
                boolean existe = false;
                try (var result = statement.executeQuery("SELECT * FROM livro WHERE 1=0")) {
                    var metadata = result.getMetaData();
                    for (int i = 1; i <= metadata.getColumnCount(); i++)
                        if ("limite_reservas".equalsIgnoreCase(metadata.getColumnName(i))) existe = true;
                }
                if (!existe) statement.executeUpdate("ALTER TABLE livro ADD COLUMN limite_reservas INTEGER NOT NULL DEFAULT 3");
            }
        };
    }
}
