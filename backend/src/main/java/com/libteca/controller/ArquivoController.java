package com.libteca.controller;

import com.libteca.service.ArquivoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import java.io.IOException;
import java.util.Map;

@RestController
public class ArquivoController {
    private final ArquivoService arquivos;
    public ArquivoController(ArquivoService arquivos){this.arquivos=arquivos;}
    @PostMapping(value="/api/arquivos",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> enviar(@RequestParam MultipartFile arquivo,@RequestParam String tipo) throws IOException {
        return Map.of("caminho",arquivos.salvar(arquivo,tipo));
    }
    @GetMapping("/capas/{nome:.+}")
    public ResponseEntity<Resource> capa(@PathVariable String nome) throws IOException {
        return ResponseEntity.ok().contentType(nome.endsWith(".png")?MediaType.IMAGE_PNG:MediaType.IMAGE_JPEG).body(arquivos.capa(nome));
    }
    @GetMapping("/api/arquivos/leitura/{id}")
    public ResponseEntity<Resource> ler(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=livro.pdf")
                .cacheControl(CacheControl.noStore()).body(arquivos.leitura(id));
    }
}
