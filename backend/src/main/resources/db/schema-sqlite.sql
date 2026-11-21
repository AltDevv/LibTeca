-- Estrutura SQLite correspondente ao banco_LibTeca.mwb.

-- INTEGER PRIMARY KEY gera o ID no SQLite; as FKs são declaradas na criação.

CREATE TABLE IF NOT EXISTS "autor" (
    "idautor" INTEGER PRIMARY KEY AUTOINCREMENT,
    "nome_autor" VARCHAR(45) NOT NULL
);

CREATE TABLE IF NOT EXISTS "categoria" (
    "idcategoria" INTEGER PRIMARY KEY AUTOINCREMENT,
    "tipo_categoria" VARCHAR(45) NOT NULL
);

CREATE TABLE IF NOT EXISTS "editora" (
    "ideditora" INTEGER PRIMARY KEY AUTOINCREMENT,
    "nome_editora" VARCHAR(45) NOT NULL
);

CREATE TABLE IF NOT EXISTS "emprestimo" (
    "idemprestimo" INTEGER PRIMARY KEY AUTOINCREMENT,
    "idusuario" BIGINT NOT NULL,
    "idexemplar" BIGINT NOT NULL,
    "idfuncionario" BIGINT,
    "status_emprestimo" VARCHAR(9) NOT NULL,
    "data_emprestimo" DATE NOT NULL,
    "data_expiracao" DATE NOT NULL,
    "prazo_escolhido" VARCHAR(12) NULL,
    "data_devolucao" DATE,
    FOREIGN KEY ("idusuario") REFERENCES "usuario" ("idusuario") ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY ("idexemplar") REFERENCES "exemplar" ("idexemplar") ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY ("idfuncionario") REFERENCES "funcionario" ("idfuncionario") ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IF NOT EXISTS "fk_Emprestimo_Usuario1_idx" ON "emprestimo" ("idusuario");

CREATE INDEX IF NOT EXISTS "fk_Emprestimo_exemplar1_idx" ON "emprestimo" ("idexemplar");

CREATE INDEX IF NOT EXISTS "fk_Emprestimo_Funcionario1_idx" ON "emprestimo" ("idfuncionario");

CREATE TABLE IF NOT EXISTS "exemplar" (
    "idexemplar" INTEGER PRIMARY KEY AUTOINCREMENT,
    "idlivro" BIGINT NOT NULL,
    "status_exemplar" VARCHAR(45) NOT NULL,
    "tipo_exemplar" VARCHAR(45) NOT NULL,
    "data_aquisicao" DATE NOT NULL,
    "caminho_arquivo" VARCHAR(255),
    "codigo_patrimonio" VARCHAR(60),
    UNIQUE ("codigo_patrimonio"),
    FOREIGN KEY ("idlivro") REFERENCES "livro" ("idlivro") ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS "fk_exemplar_Livro1_idx" ON "exemplar" ("idlivro");

CREATE TABLE IF NOT EXISTS "reserva" (
    "idreserva" INTEGER PRIMARY KEY AUTOINCREMENT,
    "idusuario" BIGINT NOT NULL,
    "idexemplar" BIGINT NOT NULL,
    "idfuncionario" BIGINT,
    "data_reserva" DATE NOT NULL,
    "status_reserva" VARCHAR(25) NOT NULL,
    FOREIGN KEY ("idusuario") REFERENCES "usuario" ("idusuario") ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY ("idexemplar") REFERENCES "exemplar" ("idexemplar") ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY ("idfuncionario") REFERENCES "funcionario" ("idfuncionario") ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IF NOT EXISTS "fk_Reserva_Usuario1_idx" ON "reserva" ("idusuario");

CREATE INDEX IF NOT EXISTS "fk_Reserva_Exemplar1_idx" ON "reserva" ("idexemplar");

CREATE INDEX IF NOT EXISTS "fk_Reserva_Funcionario1_idx" ON "reserva" ("idfuncionario");

CREATE TABLE IF NOT EXISTS "livro" (
    "idlivro" INTEGER PRIMARY KEY AUTOINCREMENT,
    "ideditora" BIGINT NOT NULL,
    "idfuncionario" BIGINT NOT NULL,
    "capa" VARCHAR(255),
    "titulo" VARCHAR(150) NOT NULL,
    "numero_de_paginas" INT NOT NULL,
    "data_lancamento" DATE NOT NULL,
    FOREIGN KEY ("ideditora") REFERENCES "editora" ("ideditora") ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY ("idfuncionario") REFERENCES "funcionario" ("idfuncionario") ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IF NOT EXISTS "fk_Livro_Editora1_idx" ON "livro" ("ideditora");

CREATE INDEX IF NOT EXISTS "fk_Livro_Funcionario1_idx" ON "livro" ("idfuncionario");

CREATE TABLE IF NOT EXISTS "usuario" (
    "idusuario" INTEGER PRIMARY KEY AUTOINCREMENT,
    "nome_usuario" VARCHAR(70) NOT NULL,
    "email" VARCHAR(100) NOT NULL,
    "senha" VARCHAR(255) NOT NULL,
    "perfil_acesso" VARCHAR(11) NOT NULL,
    UNIQUE ("email")
);

CREATE TABLE IF NOT EXISTS "livro_categoria" (
    "idlivro" BIGINT NOT NULL,
    "idcategoria" BIGINT NOT NULL,
    PRIMARY KEY ("idlivro", "idcategoria"),
    FOREIGN KEY ("idlivro") REFERENCES "livro" ("idlivro") ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY ("idcategoria") REFERENCES "categoria" ("idcategoria") ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS "fk_livro_categoria_Livro1_idx" ON "livro_categoria" ("idlivro");

CREATE INDEX IF NOT EXISTS "fk_livro_categoria_Categoria1_idx" ON "livro_categoria" ("idcategoria");

CREATE TABLE IF NOT EXISTS "telefone" (
    "idtelefone" INTEGER PRIMARY KEY AUTOINCREMENT,
    "idusuario" BIGINT NOT NULL,
    "numero" VARCHAR(11) NOT NULL,
    UNIQUE ("numero"),
    FOREIGN KEY ("idusuario") REFERENCES "usuario" ("idusuario") ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS "fk_telefone_Usuario1_idx" ON "telefone" ("idusuario");

CREATE TABLE IF NOT EXISTS "livro_autor" (
    "idlivro" BIGINT NOT NULL,
    "idautor" BIGINT NOT NULL,
    PRIMARY KEY ("idlivro", "idautor"),
    FOREIGN KEY ("idlivro") REFERENCES "livro" ("idlivro") ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY ("idautor") REFERENCES "autor" ("idautor") ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS "fk_livro_autor_Livro1_idx" ON "livro_autor" ("idlivro");

CREATE INDEX IF NOT EXISTS "fk_livro_autor_Autor1_idx" ON "livro_autor" ("idautor");

CREATE TABLE IF NOT EXISTS "funcionario" (
    "idfuncionario" INTEGER PRIMARY KEY AUTOINCREMENT,
    "idusuario" BIGINT NOT NULL,
    "data_admissao" DATE NOT NULL,
    "ativo" TINYINT NOT NULL,
    UNIQUE ("idusuario"),
    FOREIGN KEY ("idusuario") REFERENCES "usuario" ("idusuario") ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IF NOT EXISTS "fk_Funcionario_Usuario1_idx" ON "funcionario" ("idusuario");
