-- LibTeca: estrutura inicial para MySQL 8.4, sem acervo ou contas de exemplo.
-- Execute uma vez em um schema NOVO e VAZIO selecionado no Workbench.
-- Exemplo de preparação: CREATE DATABASE libteca CHARACTER SET utf8mb4; USE libteca;
-- Não é uma migração de bancos existentes. Não contém DROP, DELETE nem desativa FKs.
-- A identidade da biblioteca fica em data/biblioteca.properties, fora do banco.

CREATE TABLE `autor` (
    `idautor` BIGINT NOT NULL AUTO_INCREMENT,
    `nome_autor` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`idautor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `categoria` (
    `idcategoria` BIGINT NOT NULL AUTO_INCREMENT,
    `tipo_categoria` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`idcategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `editora` (
    `ideditora` BIGINT NOT NULL AUTO_INCREMENT,
    `nome_editora` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`ideditora`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `emprestimo` (
    `idemprestimo` BIGINT NOT NULL AUTO_INCREMENT,
    `idusuario` BIGINT NOT NULL,
    `idexemplar` BIGINT NOT NULL,
    `idfuncionario` BIGINT NULL,
    `status_emprestimo` VARCHAR(9) NOT NULL,
    `data_emprestimo` DATE NOT NULL,
    `data_expiracao` DATE NOT NULL,
    `prazo_escolhido` VARCHAR(12) NULL,
    `data_devolucao` DATE NULL,
    PRIMARY KEY (`idemprestimo`),
    KEY `fk_Emprestimo_Usuario1_idx` (`idusuario`),
    KEY `fk_Emprestimo_exemplar1_idx` (`idexemplar`),
    KEY `fk_Emprestimo_Funcionario1_idx` (`idfuncionario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `exemplar` (
    `idexemplar` BIGINT NOT NULL AUTO_INCREMENT,
    `idlivro` BIGINT NOT NULL,
    `status_exemplar` VARCHAR(45) NOT NULL,
    `tipo_exemplar` VARCHAR(45) NOT NULL,
    `data_aquisicao` DATE NOT NULL,
    `caminho_arquivo` VARCHAR(255) NULL,
    `codigo_patrimonio` VARCHAR(60) NULL,
    PRIMARY KEY (`idexemplar`),
    KEY `fk_exemplar_Livro1_idx` (`idlivro`),
    UNIQUE KEY `codigoPatrimonio_UNIQUE` (`codigo_patrimonio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `reserva` (
    `idreserva` BIGINT NOT NULL AUTO_INCREMENT,
    `idusuario` BIGINT NOT NULL,
    `idexemplar` BIGINT NOT NULL,
    `idfuncionario` BIGINT NULL,
    `data_reserva` DATE NOT NULL,
    `status_reserva` VARCHAR(25) NOT NULL,
    PRIMARY KEY (`idreserva`),
    KEY `fk_Reserva_Usuario1_idx` (`idusuario`),
    KEY `fk_Reserva_Exemplar1_idx` (`idexemplar`),
    KEY `fk_Reserva_Funcionario1_idx` (`idfuncionario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `livro` (
    `idlivro` BIGINT NOT NULL AUTO_INCREMENT,
    `ideditora` BIGINT NOT NULL,
    `idfuncionario` BIGINT NOT NULL,
    `capa` VARCHAR(255) NULL,
    `titulo` VARCHAR(150) NOT NULL,
    `numero_de_paginas` INT NOT NULL,
    `data_lancamento` DATE NOT NULL,
    `limite_reservas` INT NOT NULL DEFAULT 3,
    PRIMARY KEY (`idlivro`),
    KEY `fk_Livro_Editora1_idx` (`ideditora`),
    KEY `fk_Livro_Funcionario1_idx` (`idfuncionario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `usuario` (
    `idusuario` BIGINT NOT NULL AUTO_INCREMENT,
    `nome_usuario` VARCHAR(70) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `senha` VARCHAR(255) NOT NULL,
    `perfil_acesso` VARCHAR(11) NOT NULL,
    PRIMARY KEY (`idusuario`),
    UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `livro_categoria` (
    `idlivro` BIGINT NOT NULL,
    `idcategoria` BIGINT NOT NULL,
    KEY `fk_livro_categoria_Livro1_idx` (`idlivro`),
    KEY `fk_livro_categoria_Categoria1_idx` (`idcategoria`),
    PRIMARY KEY (`idlivro`, `idcategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `telefone` (
    `idtelefone` BIGINT NOT NULL AUTO_INCREMENT,
    `idusuario` BIGINT NOT NULL,
    `numero` VARCHAR(11) NOT NULL,
    PRIMARY KEY (`idtelefone`),
    UNIQUE KEY `valor_telefone_UNIQUE` (`numero`),
    KEY `fk_telefone_Usuario1_idx` (`idusuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `livro_autor` (
    `idlivro` BIGINT NOT NULL,
    `idautor` BIGINT NOT NULL,
    KEY `fk_livro_autor_Livro1_idx` (`idlivro`),
    KEY `fk_livro_autor_Autor1_idx` (`idautor`),
    PRIMARY KEY (`idlivro`, `idautor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `funcionario` (
    `idfuncionario` BIGINT NOT NULL AUTO_INCREMENT,
    `idusuario` BIGINT NOT NULL,
    `data_admissao` DATE NOT NULL,
    `ativo` TINYINT NOT NULL,
    PRIMARY KEY (`idfuncionario`),
    KEY `fk_Funcionario_Usuario1_idx` (`idusuario`),
    UNIQUE KEY `idusuario_UNIQUE` (`idusuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `emprestimo` ADD CONSTRAINT `fk_Emprestimo_Usuario1` FOREIGN KEY (`idusuario`) REFERENCES `usuario` (`idusuario`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `emprestimo` ADD CONSTRAINT `fk_Emprestimo_exemplar1` FOREIGN KEY (`idexemplar`) REFERENCES `exemplar` (`idexemplar`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `emprestimo` ADD CONSTRAINT `fk_Emprestimo_Funcionario1` FOREIGN KEY (`idfuncionario`) REFERENCES `funcionario` (`idfuncionario`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `exemplar` ADD CONSTRAINT `fk_exemplar_Livro1` FOREIGN KEY (`idlivro`) REFERENCES `livro` (`idlivro`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `reserva` ADD CONSTRAINT `fk_Reserva_Usuario1` FOREIGN KEY (`idusuario`) REFERENCES `usuario` (`idusuario`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `reserva` ADD CONSTRAINT `fk_Reserva_Exemplar1` FOREIGN KEY (`idexemplar`) REFERENCES `exemplar` (`idexemplar`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `reserva` ADD CONSTRAINT `fk_Reserva_Funcionario1` FOREIGN KEY (`idfuncionario`) REFERENCES `funcionario` (`idfuncionario`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `livro` ADD CONSTRAINT `fk_Livro_Editora1` FOREIGN KEY (`ideditora`) REFERENCES `editora` (`ideditora`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `livro` ADD CONSTRAINT `fk_Livro_Funcionario1` FOREIGN KEY (`idfuncionario`) REFERENCES `funcionario` (`idfuncionario`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `livro_categoria` ADD CONSTRAINT `fk_livro_categoria_Livro1` FOREIGN KEY (`idlivro`) REFERENCES `livro` (`idlivro`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `livro_categoria` ADD CONSTRAINT `fk_livro_categoria_Categoria1` FOREIGN KEY (`idcategoria`) REFERENCES `categoria` (`idcategoria`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `telefone` ADD CONSTRAINT `fk_telefone_Usuario1` FOREIGN KEY (`idusuario`) REFERENCES `usuario` (`idusuario`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `livro_autor` ADD CONSTRAINT `fk_livro_autor_Livro1` FOREIGN KEY (`idlivro`) REFERENCES `livro` (`idlivro`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `livro_autor` ADD CONSTRAINT `fk_livro_autor_Autor1` FOREIGN KEY (`idautor`) REFERENCES `autor` (`idautor`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `funcionario` ADD CONSTRAINT `fk_Funcionario_Usuario1` FOREIGN KEY (`idusuario`) REFERENCES `usuario` (`idusuario`) ON DELETE NO ACTION ON UPDATE NO ACTION;
