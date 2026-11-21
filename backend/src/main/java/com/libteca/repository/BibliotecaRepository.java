package com.libteca.repository;

import com.libteca.entity.Biblioteca;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/** Configuração desta instalação, persistida fora do banco de dados. */
@Repository
public class BibliotecaRepository {
    private final Path arquivo;
    private final ReentrantLock trava = new ReentrantLock(true);
    private static class Alteracao {
        Biblioteca dados;
        byte[] anterior;
        boolean gravada;
    }
    public BibliotecaRepository(@Value("${libteca.instalacao.diretorio:./data}") String diretorio) {
        arquivo = Path.of(diretorio).toAbsolutePath().normalize().resolve("biblioteca.properties");
    }
    public boolean existeArquivo() { return Files.isRegularFile(arquivo); }
    public int bloquear() {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
            throw new IllegalStateException("A alteração da configuração exige uma transação");
        if (TransactionSynchronizationManager.hasResource(this)) return 1;
        trava.lock();
        try {
            var alteracao = new Alteracao();
            alteracao.anterior = Files.exists(arquivo) ? Files.readAllBytes(arquivo) : null;
            alteracao.dados = ler();
            TransactionSynchronizationManager.bindResource(this, alteracao);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void beforeCommit(boolean somenteLeitura) {
                    if (!somenteLeitura) {
                        escrever(alteracao.dados);
                        alteracao.gravada = true;
                    }
                }
                @Override public void afterCompletion(int status) {
                    try {
                        if (status != STATUS_COMMITTED && alteracao.gravada) {
                            if (alteracao.anterior == null) Files.deleteIfExists(arquivo);
                            else gravarBytes(alteracao.anterior);
                        }
                    } catch(IOException e) { throw new IllegalStateException("Não foi possível restaurar a configuração local", e); }
                    finally { TransactionSynchronizationManager.unbindResource(BibliotecaRepository.this); trava.unlock(); }
                }
            });
            return 1;
        } catch(IOException | RuntimeException e) {
            trava.unlock();
            throw new IllegalStateException("Não foi possível abrir a configuração local", e);
        }
    }
    public Optional<Biblioteca> findById(Long id) {
        if (!Long.valueOf(1).equals(id)) return Optional.empty();
        var alteracao = (Alteracao) TransactionSynchronizationManager.getResource(this);
        if (alteracao != null) return Optional.of(alteracao.dados);
        trava.lock();
        try { return Optional.of(ler()); }
        finally { trava.unlock(); }
    }
    public Biblioteca save(Biblioteca biblioteca) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            bloquear();
            ((Alteracao) TransactionSynchronizationManager.getResource(this)).dados = biblioteca;
        } else {
            trava.lock();
            try { escrever(biblioteca); } finally { trava.unlock(); }
        }
        return biblioteca;
    }
    public void iniciarAcervo() {
        bloquear();
        findById(1L).orElseThrow().setAcervoIniciado(1);
    }
    private Biblioteca ler() {
        var b = new Biblioteca();
        if (!Files.exists(arquivo)) return b;
        try (var in = Files.newBufferedReader(arquivo, java.nio.charset.StandardCharsets.UTF_8)) {
            var p = new Properties(); p.load(in);
            b.setNome(p.getProperty("nome", "Minha biblioteca"));
            b.setSite(p.getProperty("site"));
            b.setConfigurada(Integer.parseInt(p.getProperty("configurada", "0")));
            b.setAcervoIniciado(Integer.parseInt(p.getProperty("acervo_iniciado", "0")));
            return b;
        } catch(IOException | NumberFormatException e) { throw new IllegalStateException("Configuração local inválida: " + arquivo, e); }
    }
    private void escrever(Biblioteca b) {
        var p = new Properties();
        p.setProperty("nome", b.getNome());
        if (b.getSite() != null) p.setProperty("site", b.getSite());
        p.setProperty("configurada", b.getConfigurada().toString());
        p.setProperty("acervo_iniciado", b.getAcervoIniciado().toString());
        try (var texto = new StringWriter()) {
            p.store(texto, "Identidade local da biblioteca - incluir no backup da instalacao");
            gravarBytes(texto.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch(IOException e) { throw new IllegalStateException("Não foi possível salvar a configuração local", e); }
    }
    private void gravarBytes(byte[] bytes) throws IOException {
        Files.createDirectories(arquivo.getParent());
        Path temporario = Files.createTempFile(arquivo.getParent(), "biblioteca-", ".tmp");
        try {
            Files.write(temporario, bytes);
            try { Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporario); }
    }
}
