package com.libteca.service;

import com.libteca.entity.Biblioteca;
import com.libteca.repository.BibliotecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ConfiguracaoLocalService {
    private final BibliotecaRepository bibliotecas;
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final Path diretorio;
    public ConfiguracaoLocalService(BibliotecaRepository bibliotecas, JdbcTemplate jdbc, DataSource dataSource,
            @Value("${libteca.instalacao.diretorio:./data}") String diretorio) {
        this.bibliotecas=bibliotecas;this.jdbc=jdbc;this.dataSource=dataSource;this.diretorio=Path.of(diretorio);
    }
    public void preparar() throws Exception {
        boolean antiga=existeTabela("configuracao_biblioteca");
        if (!bibliotecas.existeArquivo()) {
            var b=new Biblioteca();
            if (antiga) {
                jdbc.query("SELECT nome,site,configurada,acervo_iniciado FROM configuracao_biblioteca WHERE id=1", rs -> {
                    b.setNome(rs.getString("nome"));b.setSite(rs.getString("site"));
                    b.setConfigurada(rs.getInt("configurada"));b.setAcervoIniciado(rs.getInt("acervo_iniciado"));
                });
            } else {
                // Após DROP, preserva o acesso do administrador existente; a identidade pode ser editada na tela.
                b.setConfigurada(jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE perfil_acesso IN ('ADMIN','ADMIM')",Integer.class)>0?1:0);
            }
            if (jdbc.queryForObject("SELECT COUNT(*) FROM livro",Integer.class)>0) b.setAcervoIniciado(1);
            bibliotecas.save(b);
        }
        if (antiga) removerComBackup("configuracao_biblioteca");
        if (existeTabela("solicitacao_funcionario")) removerComBackup("solicitacao_funcionario");
    }
    private boolean existeTabela(String nome) throws Exception {
        try(var c=dataSource.getConnection();var rs=c.getMetaData().getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})) {
            while(rs.next()) if(nome.equalsIgnoreCase(rs.getString("TABLE_NAME"))) return true;
            return false;
        }
    }
    private void removerComBackup(String tabela) throws Exception {
        Path pasta=diretorio.resolve("migracoes");Files.createDirectories(pasta);
        Path backup=pasta.resolve(tabela+"-"+UUID.randomUUID()+".csv");
        try(var out=Files.newBufferedWriter(backup,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);
                var c=dataSource.getConnection();var s=c.createStatement();var rs=s.executeQuery("SELECT * FROM "+tabela)) {
            var m=rs.getMetaData();int colunas=m.getColumnCount();
            for(int i=1;i<=colunas;i++){if(i>1)out.write(',');out.write(csv(m.getColumnName(i)));}out.newLine();
            while(rs.next()) {
                for(int i=1;i<=colunas;i++){if(i>1)out.write(',');String valor=rs.getString(i);if(valor!=null)out.write(csv(valor));}out.newLine();
            }
        }
        // Somente as duas tabelas auxiliares descontinuadas, depois de salvar os dados locais.
        jdbc.execute("DROP TABLE "+tabela);
    }
    private String csv(String valor){return "\""+valor.replace("\"","\"\"")+"\"";}
}
