package com.libteca.service;

import com.libteca.entity.*;
import com.libteca.dto.biblioteca.*;
import com.libteca.repository.*;
import com.libteca.enums.Role;
import com.libteca.config.InstalacaoConfig;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service
public class BibliotecaService {
    private final BibliotecaRepository bibliotecas;
    private final UsuarioRepository usuarios;
    private final FuncionarioRepository funcionarios;
    private final PasswordEncoder encoder;
    private final InstalacaoConfig instalacao;
    public BibliotecaService(BibliotecaRepository bibliotecas, UsuarioRepository usuarios,
            FuncionarioRepository funcionarios, PasswordEncoder encoder, InstalacaoConfig instalacao) {
        this.bibliotecas=bibliotecas;this.usuarios=usuarios;this.funcionarios=funcionarios;
        this.encoder=encoder;this.instalacao=instalacao;
    }
    public boolean possuiAdministrador() {
        return usuarios.existsByRoleIn(List.of(Role.ADMIN,Role.ADMIM));
    }
    @Transactional(readOnly=true)
    public BibliotecaResponse informacoes() {
        Biblioteca b=bibliotecas.findById(1L).orElseThrow();
        return new BibliotecaResponse(b.getNome(),b.getSite(),b.getConfigurada()==1,possuiAdministrador(),b.getAcervoIniciado()==1);
    }
    public void exigirConfigurada() {
        if (!informacoes().configurada()) throw new ResponseStatusException(HttpStatus.CONFLICT,"O responsável precisa configurar a biblioteca antes dos cadastros");
    }
    private Biblioteca bloquear() {
        bibliotecas.bloquear();
        Biblioteca b=bibliotecas.findById(1L).orElseThrow();
        return b;
    }
    @Transactional
    public BibliotecaResponse instalar(InstalacaoRequest request) {
        Biblioteca b=bloquear();
        if(b.getConfigurada()==1) throw new ResponseStatusException(HttpStatus.CONFLICT,"Esta biblioteca já foi configurada");
        if(possuiAdministrador()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe administrador. Entre com essa conta para concluir a configuração");
        instalacao.validar(request.codigo());
        if(usuarios.existsByEmail(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Este email já está cadastrado. Use outro email para o administrador");
        Usuario usuario=new Usuario();
        usuario.setNome(request.nomeAdministrador().trim());usuario.setEmail(request.email().trim());
        usuario.setSenha(encoder.encode(request.senha()));usuario.setRole(Role.ADMIN);usuarios.save(usuario);
        Funcionario funcionario=new Funcionario();
        funcionario.setUsuario(usuario);funcionario.setDataAdmissao(LocalDate.now());funcionario.setAtivo(true);
        funcionarios.save(funcionario);
        aplicar(b,request.biblioteca());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { instalacao.invalidar(); }
        });
        return new BibliotecaResponse(b.getNome(),b.getSite(),true,true,b.getAcervoIniciado()==1);
    }
    @Transactional
    public BibliotecaResponse atualizar(BibliotecaRequest request) {
        Biblioteca b=bloquear();
        aplicar(b,request);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { instalacao.invalidar(); }
        });
        return new BibliotecaResponse(b.getNome(),b.getSite(),true,possuiAdministrador(),b.getAcervoIniciado()==1);
    }
    private void aplicar(Biblioteca b,BibliotecaRequest request) {
        b.setNome(request.nome().trim());b.setSite(request.site());b.setConfigurada(1);
    }
}
