# Teleconsulta – Gerenciamento de Salas de Espera

Sistema web (JSF + PrimeFaces) para gerenciamento de **usuários**, **pacientes**, **unidades de saúde**, **salas** e **reservas de teleconsulta**.  
Stack: **Jakarta EE 10** no **WildFly 34**, **Hibernate/JPA** e **SQLite**. Empacotado como **WAR** e executado via **Docker**.

## Sumário
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Banco de dados (SQLite) e JPA](#banco-de-dados-sqlite-e-jpa)
- [Como rodar com Docker](#como-rodar-com-docker)

---

## Arquitetura

**Padrão:** MVC com camadas separadas.

- **View (JSF/PrimeFaces):** páginas Facelets (`.xhtml`) para CRUD e filtros (ex.: `usuarios.xhtml`).
- **Web/Controller:** *managed beans* `@Named` + `@ViewScoped` orquestrando ações da tela (ex.: `UsuarioMB`).
- **Service:** EJBs `@Stateless` com regras e transações JPA (ex.: `UsuarioService`, inclui Criteria API para filtro por período).
- **Domain:** entidades JPA com Bean Validation (ex.: `Usuario`).
- **Empacotamento:** WAR (`teleconsulta.war`) com `jakartaee-web-api` em `provided`.

---

## Tecnologias

- **Java 17**
- **Maven 3.9.11**
- **WildFly 34.0.1.Final** (imagem base JDK 21)
- **Jakarta EE 10 Web Profile**
- **PrimeFaces 13 (Jakarta)**
- **Hibernate ORM 6.6.x** + `hibernate-community-dialects` (usa `SQLiteDialect`)
- **Jakarta Bean Validation**
- **SQLite** via `org.xerial:sqlite-jdbc`
- **Docker**

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
        │       │   └── Usuario.java                     # Entidade JPA + Bean Validation
        │       ├── service/
        │       │   └── UsuarioService.java              # EJB @Stateless (CRUD, cpfExiste, buscarPorPeriodo)
        │       └── web/
        │           └── UsuarioMB.java                   # @Named @ViewScoped (listar, novo, editar, salvar, excluir, filtrar)
        ├── resources/
        │   └── META-INF/
        │       └── persistence.xml                      # PU teleconsultaPU (JNDI do DataSource + Dialect SQLite)
        ├── webapp/                                      # CRUD
        └── docker/
            ├── sqlite-ds.cli                            # Script CLI: registra driver JDBC e DataSource no WildFly
            └── wildfly/
                └── modules/
                    └── org/
                        └── sqlite/
                            └── main/
                                ├── module.xml           # Define módulo org.sqlite

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
mvn clean package -DskipTests
docker build -t teleconsulta .
docker run --name teleconsulta -p 8080:8080 -d teleconsulta

URL principal
Usuários: http://localhost:8080/teleconsulta/usuarios.xhtml
