# 📚 LibTeca

O **LibTeca** é um sistema de gerenciamento de bibliotecas desenvolvido como projeto de estudos utilizando **Java + Spring Boot** no backend e **Flutter** no frontend.

O objetivo do projeto é aplicar conceitos de desenvolvimento de software utilizados em aplicações reais, incluindo arquitetura em camadas, API REST, persistência de dados, regras de negócio, autenticação e integração entre frontend e backend.

---

# 🚀 Objetivos do Projeto

Durante o desenvolvimento do LibTeca são estudados diversos conceitos, entre eles:

- Desenvolvimento de APIs REST
- Arquitetura em camadas
- Boas práticas de orientação a objetos
- Persistência de dados utilizando JPA/Hibernate
- Relacionamentos entre entidades
- Validação de dados
- Tratamento centralizado de exceções
- Regras de negócio
- Autenticação e autorização utilizando JWT (em desenvolvimento)
- Integração entre backend e frontend

---

# 🛠 Tecnologias

## Backend

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Validation
- Lombok
- H2 Database
- PostgreSQL (suporte preparado)

## Frontend

- Flutter
- Dart
- Material Design
- HTTP Client

---

# 📁 Estrutura do Backend

O backend segue uma arquitetura em camadas para separar responsabilidades.

```
controller
│
├── Recebe as requisições HTTP
├── Valida os dados recebidos
└── Chama os Services

service
│
├── Implementa as regras de negócio
├── Consulta os repositórios
├── Realiza validações
└── Converte DTOs utilizando os Mappers

repository
│
├── Comunicação com o banco de dados
└── Utiliza Spring Data JPA

entity
│
└── Representação das tabelas do banco

dto
│
├── Objetos utilizados nas requisições
└── Objetos utilizados nas respostas

mapper
│
└── Conversão entre Entity e DTO

exception
│
└── Tratamento global das exceções da API

config
│
└── Configurações gerais da aplicação

enums
│
└── Tipos utilizados pela aplicação (Roles, Status etc.)
```

---

# 📚 Entidades atuais

O sistema atualmente possui as seguintes entidades:

- Livro
- Usuário
- Autor
- Categoria
- Editora
- Reserva
- Empréstimo

---

# 🔄 Funcionalidades implementadas

## Livros

- Cadastro
- Consulta
- Atualização
- Exclusão
- Validação dos dados

## Usuários

- Cadastro
- Consulta
- Atualização
- Exclusão
- Validação de e-mail
- Controle de perfil (ROLE)

## Reservas

- Cadastro de reservas
- Cancelamento de reservas
- Controle de reservas ativas
- Regra de limite de reservas por quantidade disponível de livros

## Empréstimos

- CRUD de empréstimos
- Relacionamento com usuário e livro

## Cadastro auxiliar

- Autores
- Categorias
- Editoras

---

# 🗄 Banco de Dados

O projeto utiliza o Spring Data JPA juntamente com Hibernate para realizar o mapeamento objeto-relacional.

Os relacionamentos entre entidades são implementados utilizando anotações como:

- @ManyToOne
- @JoinColumn
- @Entity
- @Id

Sempre que possível são utilizados os métodos automáticos do Spring Data JPA, como:

```java
findById()

findAll()

save()

deleteById()

existsById()
```

Também são criadas consultas personalizadas através da convenção de nomes do Spring Data ou utilizando SQL/JPQL quando necessário.

---

# 🔐 Segurança

O projeto já possui preparação para implementação de autenticação baseada em JWT.

A ideia é possuir diferentes perfis de acesso através de Roles, permitindo separar permissões entre usuários comuns e funcionários da biblioteca.

Exemplo:

- USER
- FUNCIONARIO
- ADMIN

---

# 🎯 Objetivos de aprendizado

Durante o desenvolvimento deste projeto são praticados:

- Java moderno
- Spring Boot
- REST APIs
- Hibernate/JPA
- SQL
- Arquitetura em camadas
- DTO Pattern
- Mapper Pattern
- Repository Pattern
- Tratamento de exceções
- Validação de dados
- Regras de negócio
- Relacionamentos entre entidades
- Integração com Flutter
- Git e GitHub

---

# 📌 Próximas funcionalidades

- Autenticação JWT
- Login
- Controle de permissões por Role
- Renovação de empréstimos
- Histórico de empréstimos
- Dashboard administrativo
- Pesquisa avançada
- Upload de imagens de capa
- Notificações de devolução
- Integração completa com o frontend Flutter

---

# 👨‍💻 Autor

Projeto desenvolvido por **Vítor Nathan Cavalcanta Damasio** como parte dos estudos em desenvolvimento de software, com foco em Java, Spring Boot, arquitetura de aplicações e integração com Flutter.
