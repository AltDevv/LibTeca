package com.libteca.service;

import com.libteca.config.ArquivoConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;

@Service
public class ExclusaoArquivosService {
    private final ArquivoConfig config;
    public ExclusaoArquivosService(ArquivoConfig config){this.config=config;}
    public void preparar() throws IOException {
        Path raiz=Path.of(config.getDiretorio()).toAbsolutePath().normalize();
        if(!Files.exists(raiz))return;
        raiz=raiz.toRealPath();
        Path temporario=raiz.resolve(".exclusao-"+UUID.randomUUID()).normalize();
        if(!temporario.startsWith(raiz))throw new IOException("Destino de exclusão inválido");
        var movidos=new LinkedHashMap<Path,Path>();
        for(String pasta:List.of("capas","livros")){
            Path origem=raiz.resolve(pasta);
            if(Files.exists(origem,LinkOption.NOFOLLOW_LINKS)&&
                    (Files.isSymbolicLink(origem)||!Files.isDirectory(origem)||!origem.toRealPath().startsWith(raiz)))
                throw new IOException("Pasta de arquivos inválida");
        }
        Files.createDirectory(temporario);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCompletion(int status){
                try{
                    if(status!=STATUS_COMMITTED)for(var item:movidos.entrySet())Files.move(item.getValue(),item.getKey());
                    // walk não segue links simbólicos; somente a pasta temporária validada é removida.
                    try(var caminhos=Files.walk(temporario)){
                        for(Path p:caminhos.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);
                    }
                }catch(IOException erro){org.slf4j.LoggerFactory.getLogger(getClass()).error("Verifique os arquivos da exclusão em {}",temporario,erro);}
            }
        });
        for(String pasta:List.of("capas","livros")){
            Path origem=raiz.resolve(pasta);
            if(Files.exists(origem)){Path destino=temporario.resolve(pasta);Files.move(origem,destino);movidos.put(origem,destino);}
        }
    }
}
