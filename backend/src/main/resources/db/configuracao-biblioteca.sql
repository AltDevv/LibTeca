-- Uma configuração por instalação; não representa várias bibliotecas no mesmo servidor.
CREATE TABLE IF NOT EXISTS configuracao_biblioteca (
    id BIGINT NOT NULL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    site VARCHAR(255) NULL,
    configurada INT NOT NULL DEFAULT 0,
    acervo_iniciado INT NOT NULL DEFAULT 0
);
INSERT INTO configuracao_biblioteca (id, nome, configurada, acervo_iniciado)
SELECT 1, 'Minha biblioteca', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM configuracao_biblioteca WHERE id = 1);
UPDATE configuracao_biblioteca SET acervo_iniciado = 1
WHERE id = 1 AND EXISTS (SELECT 1 FROM livro);
