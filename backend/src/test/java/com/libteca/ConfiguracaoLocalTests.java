package com.libteca;

import com.libteca.repository.BibliotecaRepository;
import com.libteca.service.ConfiguracaoLocalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.*;
import java.nio.file.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoLocalTests {
    @TempDir Path pasta;
    DriverManagerDataSource fonte;
    JdbcTemplate jdbc;
    BibliotecaRepository configuracao;
    ConfiguracaoLocalService migracao;
    void preparar(String banco) {
        fonte=new DriverManagerDataSource(banco.equals("sqlite")?"jdbc:sqlite:"+pasta.resolve("teste.db"):
                "jdbc:h2:mem:"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1");
        jdbc=new JdbcTemplate(fonte);
        jdbc.execute("CREATE TABLE usuario(idusuario BIGINT PRIMARY KEY,perfil_acesso VARCHAR(11))");
        jdbc.execute("CREATE TABLE livro(idlivro BIGINT PRIMARY KEY)");
        jdbc.update("INSERT INTO usuario VALUES(1,'ADMIN')");jdbc.update("INSERT INTO livro VALUES(1)");
        configuracao=new BibliotecaRepository(pasta.toString());
        migracao=new ConfiguracaoLocalService(configuracao,jdbc,fonte,pasta.toString());
    }
    void tabelasAntigas() {
        jdbc.execute("CREATE TABLE configuracao_biblioteca(id BIGINT PRIMARY KEY,nome VARCHAR(100),site VARCHAR(255),configurada INT,acervo_iniciado INT)");
        jdbc.update("INSERT INTO configuracao_biblioteca VALUES(1,?,?,1,1)","Biblioteca São João","https://biblioteca.exemplo.com");
        jdbc.execute("CREATE TABLE solicitacao_funcionario(id BIGINT PRIMARY KEY,status VARCHAR(20))");
        jdbc.update("INSERT INTO solicitacao_funcionario VALUES(1,'PENDENTE')");
    }
    boolean existe(String nome)throws Exception {
        try(var c=fonte.getConnection();var rs=c.getMetaData().getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})) {
            while(rs.next()) if(nome.equalsIgnoreCase(rs.getString("TABLE_NAME")))return true;
            return false;
        }
    }
    @ParameterizedTest @ValueSource(strings={"sqlite","h2"})
    void migraIdentidadePreservaAcervoESalvaPedidosAntesDoDrop(String banco)throws Exception {
        preparar(banco);tabelasAntigas();migracao.preparar();
        assertFalse(existe("configuracao_biblioteca"));assertFalse(existe("solicitacao_funcionario"));
        var reaberta=new BibliotecaRepository(pasta.toString()).findById(1L).orElseThrow();
        assertEquals("Biblioteca São João",reaberta.getNome());assertEquals("https://biblioteca.exemplo.com",reaberta.getSite());
        assertEquals(1,reaberta.getConfigurada());assertEquals(1,reaberta.getAcervoIniciado());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM livro",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM usuario",Integer.class));
        try(var backups=Files.list(pasta.resolve("migracoes"))) {
            var arquivos=backups.toList();assertEquals(2,arquivos.size());
            for(var arquivo:arquivos)assertTrue(Files.readString(arquivo).lines().count()>=2);
        }
        reaberta.setNome("Nome atualizado");configuracao.save(reaberta);migracao.preparar();
        assertEquals("Nome atualizado",configuracao.findById(1L).orElseThrow().getNome());
    }
    @ParameterizedTest @ValueSource(strings={"sqlite","h2"})
    void tabelaJaApagadaReconheceAdministradorSemCriarOutra(String banco)throws Exception {
        preparar(banco);migracao.preparar();var info=configuracao.findById(1L).orElseThrow();
        assertEquals(1,info.getConfigurada());assertEquals(1,info.getAcervoIniciado());
        assertEquals("Minha biblioteca",info.getNome());assertFalse(existe("configuracao_biblioteca"));
    }
    @ParameterizedTest @ValueSource(strings={"sqlite","h2"})
    void primeiraInstalacaoContinuaSemConfiguracao(String banco)throws Exception {
        preparar(banco);jdbc.update("DELETE FROM usuario");jdbc.update("DELETE FROM livro");migracao.preparar();
        assertEquals(0,configuracao.findById(1L).orElseThrow().getConfigurada());
        assertEquals(0,configuracao.findById(1L).orElseThrow().getAcervoIniciado());
    }
    @Test void falhaAoSalvarArquivoNaoApagaTabelas()throws Exception {
        preparar("sqlite");tabelasAntigas();Path invalido=pasta.resolve("nao-e-pasta");Files.writeString(invalido,"teste");
        var falha=new ConfiguracaoLocalService(new BibliotecaRepository(invalido.toString()),jdbc,fonte,invalido.toString());
        assertThrows(Exception.class,falha::preparar);
        assertTrue(existe("configuracao_biblioteca"));assertTrue(existe("solicitacao_funcionario"));
    }
    @Test void rollbackRestauraArquivoMesmoAposGravacaoAntesDoCommit()throws Exception {
        preparar("h2");migracao.preparar();byte[] antes=Files.readAllBytes(pasta.resolve("biblioteca.properties"));
        var transacao=new TransactionTemplate(new DataSourceTransactionManager(fonte));
        assertThrows(IllegalStateException.class,()->transacao.execute(status->{
            configuracao.bloquear();configuracao.findById(1L).orElseThrow().setNome("Não salvar");jdbc.update("DELETE FROM livro");
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
                @Override public void beforeCommit(boolean leitura){throw new IllegalStateException("Falha simulada no commit");}
            });return null;
        }));
        assertArrayEquals(antes,Files.readAllBytes(pasta.resolve("biblioteca.properties")));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM livro",Integer.class));
    }
}
