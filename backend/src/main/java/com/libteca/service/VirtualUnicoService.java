package com.libteca.service;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.util.*;

@Component
@Profile("!instalacao-banco")
public class VirtualUnicoService implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transacao;
    private final Path pasta;
    public VirtualUnicoService(JdbcTemplate jdbc, PlatformTransactionManager manager,
            @Value("${libteca.instalacao.diretorio:./data}") String diretorio) {
        this.jdbc=jdbc;this.transacao=new TransactionTemplate(manager);this.pasta=Path.of(diretorio,"migracoes");
    }
    @Override public void run(ApplicationArguments args) { consolidar(); }

    public void consolidar() {
        transacao.executeWithoutResult(status->{
            var linhas=jdbc.queryForList("select * from exemplar where tipo_exemplar='VIRTUAL' order by idexemplar");
            var grupos=new LinkedHashMap<Long,List<Map<String,Object>>>();
            for(var linha:linhas) grupos.computeIfAbsent(((Number)linha.get("idlivro")).longValue(),k->new ArrayList<>()).add(linha);
            boolean mudou=grupos.values().stream().anyMatch(g->g.size()>1||g.stream().anyMatch(e->List.of("EMPRESTADO","RESERVADO").contains(e.get("status_exemplar"))));
            if(!mudou)return;
            for(var grupo:grupos.values()) {
                long arquivos=grupo.stream().map(e->e.get("caminho_arquivo")).filter(Objects::nonNull).filter(a->!a.toString().isBlank()).distinct().count();
                if(arquivos>1)throw new IllegalStateException("O livro "+grupo.get(0).get("idlivro")+" possui PDFs diferentes; revise antes de consolidar");
            }
            try {
                Files.createDirectories(pasta);
                var backup=Map.of("exemplares",linhas,"emprestimos",jdbc.queryForList("select p.* from emprestimo p join exemplar e on e.idexemplar=p.idexemplar where e.tipo_exemplar='VIRTUAL'"),
                        "reservas",jdbc.queryForList("select r.* from reserva r join exemplar e on e.idexemplar=r.idexemplar where e.tipo_exemplar='VIRTUAL'"));
                Files.writeString(pasta.resolve("virtuais-"+System.currentTimeMillis()+".json"),new tools.jackson.databind.ObjectMapper().writeValueAsString(backup),StandardOpenOption.CREATE_NEW);
            } catch(Exception e) {throw new IllegalStateException("Não foi possível salvar o backup dos virtuais",e);}
            for(var grupo:grupos.values()) {
                long id=((Number)grupo.get(0).get("idexemplar")).longValue();
                String arquivo=grupo.stream().map(e->e.get("caminho_arquivo")).filter(Objects::nonNull).map(Object::toString).filter(a->!a.isBlank()).findFirst().orElse(null);
                for(int i=1;i<grupo.size();i++) {
                    Object antigo=grupo.get(i).get("idexemplar");
                    jdbc.update("update emprestimo set idexemplar=? where idexemplar=?",id,antigo);
                    jdbc.update("update reserva set idexemplar=? where idexemplar=?",id,antigo);
                    jdbc.update("delete from exemplar where idexemplar=?",antigo);
                }
                jdbc.update("update exemplar set caminho_arquivo=?,status_exemplar=? where idexemplar=?",arquivo,arquivo==null?"INDISPONIVEL":"DISPONIVEL",id);
            }
        });
    }
}
