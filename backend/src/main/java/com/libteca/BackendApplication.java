package com.libteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import com.libteca.config.BancoInicialConfig;
import java.util.*;
import java.nio.file.*;
import org.springframework.core.env.PropertiesPropertySource;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {
    private static String[] argumentos = new String[0];

	public static void main(String[] args) {
        argumentos = args.clone();
        iniciar(args);
	}

    private static void iniciar(String[] args) {
        SpringApplication app = new SpringApplication();
        app.setDefaultProperties(Map.of("spring.profiles.active", "instalacao-banco",
                "spring.profiles.include", "web", "spring.config.import",
                "optional:file:${LIBTECA_CONFIG_DIR:./data}/banco.properties"));
        app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) evento -> {
            boolean instalar = Arrays.asList(evento.getEnvironment().getActiveProfiles()).contains("instalacao-banco");
            if(!instalar) {
                Path arquivo=Path.of(evento.getEnvironment().getProperty("libteca.instalacao.diretorio","./data")).resolve("banco.properties");
                if(Files.exists(arquivo)) {
                    Properties props=new Properties();
                    try(var in=Files.newInputStream(arquivo)){props.load(in);}
                    catch(java.io.IOException e){throw new IllegalStateException("Não foi possível ler a configuração local do banco.",e);}
                    if(Arrays.asList(evento.getEnvironment().getActiveProfiles()).contains(props.getProperty("spring.profiles.active")))
                        evento.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource("banco-local",props));
                }
            }
            app.setSources(Set.of((instalar ? BancoInicialConfig.class : BackendApplication.class).getName()));
        });
        app.run(args);
    }

    public static void iniciarComBanco(String perfil) {
        var args = new ArrayList<>(Arrays.stream(argumentos)
                .filter(a -> !a.startsWith("--spring.profiles.active=")).toList());
        args.add("--spring.profiles.active=" + perfil);
        iniciar(args.toArray(String[]::new));
    }

}
