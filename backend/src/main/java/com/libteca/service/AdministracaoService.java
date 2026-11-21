package com.libteca.service;

import com.libteca.config.*;
import com.libteca.dto.biblioteca.*;
import com.libteca.entity.Usuario;
import com.libteca.enums.Role;
import com.libteca.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@Service
public class AdministracaoService {
    private final AcessoService acesso;
    private final BibliotecaRepository bibliotecas;
    private final FuncionarioRepository funcionarios;
    private final PasswordEncoder encoder;
    private final EntityManager em;
    private final JdbcTemplate jdbc;
    private final InstalacaoConfig instalacao;
    private final AdministracaoConfig.Sessoes sessoes;
    private final ExclusaoArquivosService arquivos;
    public AdministracaoService(AcessoService acesso,BibliotecaRepository bibliotecas,FuncionarioRepository funcionarios,
            PasswordEncoder encoder,EntityManager em,JdbcTemplate jdbc,InstalacaoConfig instalacao,
            AdministracaoConfig.Sessoes sessoes,ExclusaoArquivosService arquivos){
        this.acesso=acesso;this.bibliotecas=bibliotecas;this.funcionarios=funcionarios;this.encoder=encoder;
        this.em=em;this.jdbc=jdbc;this.instalacao=instalacao;this.sessoes=sessoes;this.arquivos=arquivos;
    }
    private Usuario validar(String senha){
        bibliotecas.bloquear();
        Usuario atual=acesso.atual();em.refresh(atual,LockModeType.PESSIMISTIC_WRITE);
        if((atual.getRole()!=Role.ADMIN&&atual.getRole()!=Role.ADMIM)
                ||funcionarios.findByUsuarioId(atual.getId()).map(f->!Boolean.TRUE.equals(f.getAtivo())).orElse(false))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Somente o administrador pode realizar esta operação");
        if(!encoder.matches(senha,atual.getSenha()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Senha atual incorreta");
        return atual;
    }
    @Transactional
    public void transferir(TransferenciaRequest request){
        Usuario atual=validar(request.senhaAtual());
        var funcionario=funcionarios.findById(request.funcionarioId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Funcionário não encontrado"));
        em.refresh(funcionario,LockModeType.PESSIMISTIC_WRITE);
        var novo=funcionario.getUsuario();em.refresh(novo,LockModeType.PESSIMISTIC_WRITE);
        if(!Boolean.TRUE.equals(funcionario.getAtivo())||novo.getRole()!=Role.FUNCIONARIO||novo.getId().equals(atual.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Escolha outro funcionário ativo");
        novo.setRole(Role.ADMIN);atual.setRole(Role.FUNCIONARIO);
        encerrarSessoesDepois();
    }
    @Transactional(rollbackFor=Exception.class)
    public void apagar(ExclusaoBibliotecaRequest request) throws java.io.IOException {
        validar(request.senhaAtual());
        var biblioteca=bibliotecas.findById(1L).orElseThrow();
        if(!biblioteca.getNome().equals(request.nomeBiblioteca()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Digite o nome da biblioteca exatamente como aparece na tela");
        arquivos.preparar();
        instalacao.gerarCodigo();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)instalacao.invalidar();}
        });
        // Ordem respeita todas as FKs. Não apaga o esquema nem reinicia IDs, evitando reutilização de sessões antigas.
        try {
            for(String tabela:List.of("reserva","emprestimo","telefone","livro_autor","livro_categoria","exemplar","livro","funcionario","usuario","autor","categoria","editora"))
                jdbc.update("DELETE FROM "+tabela);
            bibliotecas.save(new com.libteca.entity.Biblioteca());
        } catch(org.springframework.dao.DataAccessException erro) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Não foi possível apagar a biblioteca. Os dados foram preservados",erro);
        }
        em.clear();encerrarSessoesDepois();
    }
    private void encerrarSessoesDepois(){
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCommit(){sessoes.encerrarTodas();}
        });
    }
}
