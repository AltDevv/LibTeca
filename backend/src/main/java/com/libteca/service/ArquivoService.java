package com.libteca.service;

import com.libteca.config.ArquivoConfig;
import com.libteca.repository.EmprestimoRepository;
import com.libteca.enums.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ArquivoService {
    private final ArquivoConfig config;
    private final AcessoService acesso;
    private final EmprestimoRepository emprestimos;
    public ArquivoService(ArquivoConfig config,AcessoService acesso,EmprestimoRepository emprestimos){
        this.config=config;this.acesso=acesso;this.emprestimos=emprestimos;
    }
    public String salvar(MultipartFile arquivo,String tipo) throws IOException {
        if(arquivo.isEmpty() || arquivo.getSize()>config.getTamanhoMaximo()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Arquivo vazio ou maior que o limite de 20 MB");
        byte[] bytes=arquivo.getBytes();
        boolean pdf=bytes.length>4 && new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-");
        boolean png=bytes.length>8 && bytes[0]==(byte)137 && bytes[1]==80 && bytes[2]==78 && bytes[3]==71;
        boolean jpg=bytes.length>3 && bytes[0]==(byte)255 && bytes[1]==(byte)216 && bytes[2]==(byte)255;
        String pasta,ext;
        if(tipo.equals("capa") && (png||jpg)){pasta="capas";ext=png?".png":".jpg";}
        else if(tipo.equals("livro") && pdf){pasta="livros";ext=".pdf";}
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use PNG/JPG para capas e PDF para livros");
        Path raiz=Path.of(config.getDiretorio()).toAbsolutePath().normalize();
        Files.createDirectories(raiz.resolve(pasta));
        String nome=UUID.randomUUID()+ext;
        Files.write(raiz.resolve(pasta).resolve(nome),bytes,StandardOpenOption.CREATE_NEW);
        return pasta.equals("capas")?"/capas/"+nome:"livros/"+nome;
    }
    public Resource capa(String nome) throws IOException {return recurso("capas/"+nome);}
    private Resource recurso(String referencia) throws IOException {
        if(referencia==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Arquivo não cadastrado");
        Path raiz=Path.of(config.getDiretorio()).toAbsolutePath().normalize();
        Path caminho=raiz.resolve(referencia).normalize();
        if(!caminho.startsWith(raiz)||!Files.isRegularFile(caminho)||!caminho.toRealPath().startsWith(raiz.toRealPath()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Arquivo não encontrado");
        return new FileSystemResource(caminho);
    }
    @Transactional(readOnly=true)
    public Resource leitura(Long id) throws IOException {
        var emprestimo=emprestimos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        acesso.dono(emprestimo.getUsuario().getId());
        if(emprestimo.getDataDevolucao()!=null || emprestimo.getDataExpiracao().isBefore(LocalDate.now())
                || emprestimo.getStatus()!=StatusEmprestimo.ATIVO || emprestimo.getExemplar().getTipo()!=TypeLivro.VIRTUAL)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Este empréstimo não permite mais leitura");
        return recurso(emprestimo.getExemplar().getCaminhoArquivo());
    }
}
