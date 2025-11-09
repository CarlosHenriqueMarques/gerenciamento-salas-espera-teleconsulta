# Teleconsulta – Gerenciamento de Salas de Espera

Sistema web (JSF + PrimeFaces) para gerenciamento de **usuários**, **pacientes**, **unidades de saúde**, **salas** e **reservas de teleconsulta**.  
Stack: **Jakarta EE 10** no **WildFly 34**, **Hibernate/JPA** e **SQLite**. Empacotado como **WAR** e executado via **Docker**.

## Sumário
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Banco de dados (SQLite) e JPA](#banco-de-dados-sqlite-e-jpa)
- [Como rodar com Docker](#como-rodar-com-docker)
- [URLs principais](#urls-principais)

---

## Arquitetura

**Padrão:** MVC com camadas separadas.

- **View (JSF/PrimeFaces):** páginas Facelets (`.xhtml`) para CRUD e filtros (ex.: `usuarios.xhtml`).
- **Web/Controller:** *managed beans* `@Named` + `@ViewScoped` orquestrando ações da tela (ex.: `UsuarioMB`).
- **Service:** EJBs `@Stateless` com regras e transações JPA (ex.: `UsuarioService`, inclui Criteria API para filtro por período).
- **Domain:** entidades JPA com Bean Validation (ex.: `Usuario`).
- **Empacotamento:** WAR (`teleconsulta.war`) com `jakartaee-web-api` em `provided`.
- **Docker/WildFly:** WildFly 34 configurado com **módulo JDBC do SQLite** e **DataSource JNDI** via script CLI.
---

## Tecnologias

- **Java 17**
- **Maven 3.9.11**
- **WildFly 34.0.1.Final** (imagem base JDK 21)
- **Jakarta EE 10 Web Profile**
- **PrimeFaces 13 (Jakarta)**
- **Hibernate ORM 6.6.x** + `hibernate-community-dialects` (`SQLiteDialect`)
- **Jakarta Bean Validation**
- **SQLite** via `org.xerial:sqlite-jdbc`
- **Docker**

---

## Funcionalidades

- **Usuários:** CRUD, filtro por período (data de cadastro).
- **Pacientes:** CRUD, filtros por **termo** (nome/doc/contato) e **nascimento (de/até)**.
- **Unidades de Saúde:** CRUD, filtro por **termo** (nome/razão/sigla) e unicidade de **CNPJ/CNES**.
- **Salas:** CRUD, filtro por **nome** e **unidade**.
- **Reservas:**
  - CRUD com **validação de conflito de horários** por sala.
  - Filtros por **período**, **unidade**, **sala** e **usuário**.
  - **Múltiplos usuários** por reserva (N:N).
- **Contas (login):** CRUD de contas (login/perfil/ativo/senha hash) e **autenticação** no sistema.
- **Navegação:** tela inicial com atalhos; links de retorno para “Início” nas telas.

---

## Autenticação & Segurança

- **Login obrigatório**: `AuthFilter` protege todas as páginas `*.xhtml` (exceto `login.xhtml` e recursos JSF).
- **AuthController** (sessão): gerencia `login`, `logout`, estado `logado` e exibe saudação/botão **Sair** no topo das telas.
- **Conta inicial (seed):** ao subir o container, é garantida a existência de um usuário **admin / admin** (perfil ADMIN).  
  > Se desejar, altere o seed em um inicializador (ex.: `DataInitializer`) ou diretamente na tela **Contas**.
- **Senhas:** armazenadas como **hash** via `PasswordService`.

---

## Validações (CPF/CNPJ)

- Utilitários: `CpfCnpjUtils.isValidCPF(...)` e `isValidCNPJ(...)`.
- Bean Validation customizada: anotações `@CPF` e `@CNPJ` (com *validators*) aplicadas nas entidades.
- (Opcional) Validadores JSF: `cpfValidator` e `cnpjValidator` para validar no campo antes do submit.

---

## Estrutura de pastas

```text
gerenciamento-sala-espera-teleconsulta/
├── pom.xml
├── Dockerfile
└── src/
    └── main/
        ├── java/
        │   └── br/com/carlos/teleconsulta/
        │       ├── domain/
        │       │   ├── enums/                  # Enums (ex.: Perfil, Sexo, etc.)
        │       │   ├── Usuario.java
        │       │   ├── Paciente.java
        │       │   ├── UnidadeSaude.java
        │       │   ├── Sala.java
        │       │   └── Reserva.java
        │       ├── service/
        │       │   ├── UsuarioService.java
        │       │   ├── PacienteService.java
        │       │   ├── UnidadeSaudeService.java
        │       │   ├── SalaService.java
        │       │   ├── ReservaService.java
        │       │   ├── ContaService.java
        │       │   └── PasswordService.java
        │       ├── security/
        │       │   ├── controller/
        │       │   │   └── AuthController.java     # login/logout (sessão)
        │       │   └── filter/
        │       │       └── AuthFilter.java         
        │       ├── validation/
        │       │   ├── CPF.java
        │       │   ├── CNPJ.java
        │       │   ├── CPFValidator.java
        │       │   └── CNPJValidator.java
        │       ├── util/
        │       │   └── CpfCnpjUtils.java
        │       └── web/                            # Controllers de tela (JSF)
        │           ├── UsuarioController.java
        │           ├── PacienteController.java
        │           ├── UnidadeSaudeController.java
        │           ├── SalaController.java
        │           ├── ReservaController.java
        │           └── ContaController.java
        ├── resources/
        │   └── META-INF/
        │       └── persistence.xml                 # PU teleconsultaPU (DataSource + Dialect)
        ├── webapp/
        │   ├── index.xhtml
        │   ├── login.xhtml
        │   ├── usuarios.xhtml
        │   ├── pacientes.xhtml
        │   ├── unidades.xhtml
        │   ├── salas.xhtml
        │   ├── reservas.xhtml
        │   └── contas.xhtml
        └── docker/
            ├── sqlite-ds.cli                       # registra driver JDBC + DataSource no WildFly
            └── wildfly/
                └── modules/
                    └── org/
                        └── sqlite/
                            └── main/
                                ├── module.xml      # módulo org.sqlite
```
---

## Banco de dados (SQLite) e JPA

- O banco é um arquivo:  
  `teleconsulta.db` em **`$JBOSS_HOME/standalone/data`** (dentro do container).

- **Persistence Unit:** `teleconsultaPU`  
  **JNDI do DataSource:** `java:/jdbc/TeleconsultaDS`

- **Dialect:** `org.hibernate.community.dialect.SQLiteDialect`

---

## Como rodar com Docker (passo a passo)
> Pré-requisitos: **Docker**, **Java 17**, **Maven 3.9.11** instalados localmente.

1) **Build do WAR**
```bash
docker rm -f teleconsulta
mvn clean package -DskipTests
docker build -t teleconsulta .
docker run --name teleconsulta -p 8080:8080 -d teleconsulta
```
---

## Primeiros passos 

- Abra: http://localhost:8080/teleconsulta/login.xhtml
- Credenciais iniciais: admin / admin

---

## URL principal
- Login: http://localhost:8080/teleconsulta/login.xhtml
- Início: http://localhost:8080/teleconsulta/index.xhtml
- Usuários: http://localhost:8080/teleconsulta/usuarios.xhtml
- Pacientes: http://localhost:8080/teleconsulta/pacientes.xhtml
- Unidades: http://localhost:8080/teleconsulta/unidades.xhtml
- Salas: http://localhost:8080/teleconsulta/salas.xhtml
- Reservas: http://localhost:8080/teleconsulta/reservas.xhtml
- Contas (admin): http://localhost:8080/teleconsulta/contas.xhtml
---

## Melhorias
- Autorização por perfil (ex.: ADMIN/USER por tela/ação).
- Auditoria (quem criou/alterou, quando).
- Máscaras/formatadores para CPF/CNPJ/telefone na UI.
---
