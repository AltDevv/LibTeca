package com.libteca.service;

import com.libteca.BackendApplication;
import com.libteca.config.InstalacaoConfig;
import com.libteca.dto.biblioteca.BancoRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Profile("instalacao-banco")
public class BancoInicialService {
    private final Path diretorio;
    private final InstalacaoConfig codigo;
    private final ConfigurableApplicationContext contexto;
    private boolean iniciando;
    private static volatile String erro = "";

    public BancoInicialService(@Value("${libteca.instalacao.diretorio:./data}") String diretorio,
            InstalacaoConfig codigo, ConfigurableApplicationContext contexto) {
        this.diretorio=Path.of(diretorio).toAbsolutePath().normalize(); this.codigo=codigo; this.contexto=contexto;
    }
    public static String erroInicializacao() { return erro; }

    public synchronized void configurar(BancoRequest request) throws IOException {
        codigo.validar(request.codigo());
        Path arquivo=diretorio.resolve("banco.properties");
        if(iniciando || Files.exists(arquivo)) throw new ResponseStatusException(HttpStatus.CONFLICT,"O banco já foi escolhido. Reinicie o backend para continuar.");
        Files.createDirectories(diretorio);
        Properties props=propriedades(request,diretorio);
        verificar(props,request.tipo());
        salvar(arquivo,props);
        iniciando=true; erro=""; codigo.prepararReinicio(); codigo.invalidar();
        Thread reinicio=new Thread(() -> {
            try {
                Thread.sleep(700); contexto.close();
                BackendApplication.iniciarComBanco(request.tipo());
            } catch(Exception falha) {
                erro="Não foi possível iniciar com esse banco. Confira a conexão e a estrutura das tabelas e tente novamente.";
                try { Files.deleteIfExists(arquivo); BackendApplication.iniciarComBanco("instalacao-banco"); }
                catch(Exception segundaFalha) { org.slf4j.LoggerFactory.getLogger(getClass()).error("Reinicie o backend para retomar a configuração do banco."); }
            }
        },"libteca-iniciar-banco");
        reinicio.setDaemon(false); reinicio.start();
    }

    public static Properties propriedades(BancoRequest r, Path diretorio) {
        Properties p=new Properties();p.setProperty("spring.profiles.active",r.tipo());
        if("sqlite".equals(r.tipo())) {
            p.setProperty("spring.datasource.url","jdbc:sqlite:"+diretorio.resolve("libteca.db").toAbsolutePath().normalize());
        } else if("mysql".equals(r.tipo())) {
            if(r.host()==null || !r.host().matches("[a-zA-Z0-9.-]+") || r.porta()==null || r.porta()<1 || r.porta()>65535
                    || r.banco()==null || !r.banco().matches("[a-zA-Z0-9_]+") || r.usuario()==null || r.usuario().isBlank()) {
                throw new IllegalArgumentException("Informe servidor, porta, nome do banco e usuário MySQL válidos.");
            }
            p.setProperty("spring.datasource.url","jdbc:mysql://"+r.host()+":"+r.porta()+"/"+r.banco()+"?connectTimeout=5000&socketTimeout=5000");
            p.setProperty("spring.datasource.username",r.usuario());p.setProperty("spring.datasource.password",r.senha()==null?"":r.senha());
        } else throw new IllegalArgumentException("Selecione MySQL ou SQLite.");
        return p;
    }

    private void verificar(Properties props,String tipo) {
        try(Connection c=DriverManager.getConnection(props.getProperty("spring.datasource.url"),
                props.getProperty("spring.datasource.username",""),props.getProperty("spring.datasource.password",""))) {
            if("sqlite".equals(tipo)) {
                try(var consulta=c.createStatement(); var rs=consulta.executeQuery("select count(*) from sqlite_master where type='table' and name not like 'sqlite_%'")) {
                    if(rs.next() && rs.getInt(1)==0)return;
                }
            }
            String sql;
            try(var arquivo=new ClassPathResource("db/schema-sqlite.sql").getInputStream()) {
                sql=new String(arquivo.readAllBytes(),StandardCharsets.UTF_8);
            }
            var tabelas=Pattern.compile("CREATE TABLE IF NOT EXISTS \"([^\"]+)\" \\((.*?)\\);",Pattern.DOTALL).matcher(sql);
            while(tabelas.find()) {
                var campos=Pattern.compile("(?m)^\\s*\"([^\"]+)\"").matcher(tabelas.group(2));var nomes=new ArrayList<String>();
                while(campos.find())nomes.add("`"+campos.group(1)+"`");
                try(var consulta=c.createStatement()) {consulta.setQueryTimeout(5);consulta.executeQuery("SELECT "+String.join(",",nomes)+" FROM `"+tabelas.group(1)+"` WHERE 1=0").close();}
            }
        } catch(SQLException | IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível validar o banco. Confira a conexão, as credenciais e se o modelo atual do LibTeca foi importado no MySQL.");
        }
    }

    private static void salvar(Path destino,Properties props) throws IOException {
        Path temporario=Files.createTempFile(destino.getParent(),"banco-",".tmp");
        try {
            var posix=Files.getFileAttributeView(temporario,PosixFileAttributeView.class);
            if(posix!=null)posix.setPermissions(PosixFilePermissions.fromString("rw-------"));
            var acl=Files.getFileAttributeView(temporario,AclFileAttributeView.class);
            if(acl!=null)acl.setAcl(List.of(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(Files.getOwner(temporario)).setPermissions(EnumSet.allOf(AclEntryPermission.class)).build()));
            try(var out=Files.newOutputStream(temporario)){props.store(out,"Configuracao local do LibTeca - nao publicar");}
            Files.move(temporario,destino,StandardCopyOption.ATOMIC_MOVE);
        } finally {Files.deleteIfExists(temporario);}
    }
}
