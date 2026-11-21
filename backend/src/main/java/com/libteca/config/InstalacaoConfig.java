package com.libteca.config;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.LoggerFactory;

@Component
public class InstalacaoConfig {
    private final Path arquivo;
    private String codigo;
    private Instant expira;
    private record CodigoTransferido(String valor, Instant expira) {}
    private static final java.util.concurrent.atomic.AtomicReference<CodigoTransferido> transferencia = new java.util.concurrent.atomic.AtomicReference<>();
    public synchronized void prepararReinicio() { transferencia.set(new CodigoTransferido(codigo,expira)); }
    public InstalacaoConfig(@Value("${libteca.instalacao.diretorio:./data}") String diretorio) {
        arquivo = Path.of(diretorio).toAbsolutePath().normalize().resolve("instalacao.codigo");
    }
    public synchronized void gerarCodigo() throws java.io.IOException {
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        codigo = HexFormat.of().formatHex(bytes);
        expira = Instant.now().plus(Duration.ofMinutes(30));
        var anterior=transferencia.getAndSet(null);
        if(anterior!=null && anterior.valor()!=null && Instant.now().isBefore(anterior.expira())) {
            codigo=anterior.valor();expira=anterior.expira();
        }
        Files.createDirectories(arquivo.getParent());
        Files.writeString(arquivo, codigo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LoggerFactory.getLogger(getClass()).info("Configure a biblioteca no navegador. Código temporário (30 minutos) disponível somente no servidor: {}", arquivo);
    }
    public synchronized void validar(String informado) {
        if (codigo == null || !Instant.now().isBefore(expira)
                || !MessageDigest.isEqual(codigo.getBytes(StandardCharsets.UTF_8), informado.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Código de instalação inválido ou expirado. Consulte o arquivo no servidor; reiniciar gera um novo código");
        }
    }
    public synchronized void invalidar() {
        codigo = null; expira = null;
        try { Files.deleteIfExists(arquivo); }
        catch (java.io.IOException e) { LoggerFactory.getLogger(getClass()).warn("O código já foi invalidado, mas não foi possível remover o arquivo local"); }
    }
}
