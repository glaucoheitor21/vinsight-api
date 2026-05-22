# VINSight Ford API

Backend Spring Boot da plataforma **VINSight Ford** — Challenge FIAP 2026 / Ford Motor Company / Desafio 02.

## Equipe

- **Glauco Heitor Gonçalves** — RM 555978
- **Pedro Henrique Junqueira** — RM 556278

## Sobre

API REST que dá suporte à plataforma de retenção de clientes pós-venda da Ford. Implementa CRUD de **Clientes**, **Veículos**, **Agendamentos**, **Concessionárias** e **Leads** (gerados pelo modelo de IA do VINSight Core), seguindo SOA com separação clara de camadas (Controller / Service / Repository / Entity) e package-by-feature.

## Arquitetura

<img width="2400" height="1600" alt="VINSight_Ford_SpringBoot_Diagram" src="https://github.com/user-attachments/assets/de02e88a-ea60-42ec-afe2-d9401cf54ba7" />

## Stack

- **Java 21** (LTS)
- **Spring Boot 4.0.6** (com Jackson 3 e novos starters `spring-boot-starter-webmvc`)
- Spring Web · Spring Data JPA · Validation
- **MySQL 8** (banco de dados)
- **Flyway** (migrations versionadas)
- **Lombok**
- **SpringDoc OpenAPI** (Swagger UI auto-gerado)

## Pré-requisitos

- JDK 21
- MySQL 8 rodando localmente (porta padrão `3306`)
- Maven 3.9+ (ou use o `mvnw` que vem no repo)

## Como rodar

### 1. Configurar credenciais do MySQL

Edite `src/main/resources/application.properties` se o seu MySQL usa user/senha diferentes do default:

```properties
spring.datasource.username=root
spring.datasource.password=fiap
```

A URL JDBC já tem `createDatabaseIfNotExist=true`, então o schema `vinsight` é criado automaticamente na primeira execução — não precisa rodar `CREATE DATABASE` manualmente.

### 2. Subir a aplicação

```bash
./mvnw spring-boot:run
```

ou, no Windows:

```cmd
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. Flyway aplica todas as migrations em `src/main/resources/db/migration/` na ordem (V1 → V5).

## Documentação interativa

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

Todos os endpoints estão anotados com `@Operation` e `@Tag`, então o Swagger já apresenta as operações agrupadas por recurso.

## Estrutura de pacotes

```
br.com.fiap.vinsight_api/
├── controller/         REST controllers (1 por recurso)
├── cliente/            Feature package — Cliente
├── veiculo/            Feature package — Veículo
├── agendamento/        Feature package — Agendamento
├── concessionaria/     Feature package — Concessionária
├── lead/               Feature package — Lead
├── shared/             Embeddables compartilhados (Endereco, DadosContato, DadosPessoais)
├── infra/exception/    GlobalExceptionHandler + exceções de domínio
└── config/             SwaggerConfig, WebConfig (paginação)
```

Cada feature package contém: `Entity`, `Repository`, DTOs (records), `Service`, e seus enums quando aplicável.

## Endpoints — visão geral

Base path: **`/api/v1`**.

### Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/clientes` | Cadastra novo cliente |
| GET | `/clientes` | Lista clientes ativos (paginado) |
| GET | `/clientes/{id}` | Detalha cliente |
| PUT | `/clientes/{id}` | Atualiza cliente |
| DELETE | `/clientes/{id}` | Inativa cliente (soft delete) |
| GET | `/clientes/{id}/veiculos` | Lista veículos do cliente |

### Veículos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/veiculos` | Cadastra novo veículo |
| GET | `/veiculos` | Lista veículos não-inativos (paginado) |
| GET | `/veiculos/{id}` | Detalha veículo por ID |
| GET | `/veiculos/vin/{vin}` | Detalha veículo por VIN |
| PUT | `/veiculos/{id}` | Atualiza veículo |
| DELETE | `/veiculos/{id}` | Inativa veículo (status = INATIVO) |
| GET | `/veiculos/{id}/agendamentos` | Histórico de agendamentos do veículo |

### Agendamentos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/agendamentos` | Cria novo agendamento |
| GET | `/agendamentos` | Lista (filtros: `dataInicio`, `dataFim`, `concessionariaId`, `status`) |
| GET | `/agendamentos/{id}` | Detalha agendamento |
| **PATCH** | `/agendamentos/{id}/status` | Atualiza status (atualização parcial) |
| DELETE | `/agendamentos/{id}` | Cancela (status = CANCELADO) |

### Concessionárias

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/concessionarias` | Cadastra nova concessionária |
| GET | `/concessionarias` | Lista ativas (paginado) |
| GET | `/concessionarias/{id}` | Detalha concessionária |
| PUT | `/concessionarias/{id}` | Atualiza concessionária |
| DELETE | `/concessionarias/{id}` | Inativa (soft delete) |
| GET | `/concessionarias/{id}/agendamentos` | Lista agendamentos da concessionária |

### Leads

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/leads` | Registra lead gerado pelo modelo de ML |
| GET | `/leads` | Lista (filtros: `prioridade`, `status`, `clienteId`) — ordenação default por `score` desc |
| GET | `/leads/{id}` | Detalha lead |
| **PATCH** | `/leads/{id}/status` | Atualiza status (NOVO → EM_CONTATO → CONVERTIDO/PERDIDO) |
| **PATCH** | `/leads/{id}/conversao` | Marca como CONVERTIDO e registra `dataConversao` |

Detalhes completos de cada endpoint (request/response, validações, exemplos) estão no **Swagger UI**.

## Uso de métodos HTTP

| Método | Quando usamos | Idempotência |
|---|---|---|
| GET | Leituras (detalhar/listar) | Sim |
| POST | Criação de recurso (não-idempotente) | Não |
| PUT | Substituição completa do recurso | Sim |
| PATCH | Atualização **parcial** — usado em `agendamentos/{id}/status`, `leads/{id}/status`, `leads/{id}/conversao` | Sim |
| DELETE | Remoção lógica (soft delete via flag `ativo` ou mudança de status) | Sim |

## Tratamento de erros

Todas as exceções passam pelo [`GlobalExceptionHandler`](src/main/java/br/com/fiap/vinsight_api/infra/exception/GlobalExceptionHandler.java) (`@RestControllerAdvice`), que retorna JSON padronizado:

```json
{
  "timestamp": "2026-05-19T10:30:00",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Cliente com id 99 não encontrado.",
  "detalhes": null
}
```

Em validações de body, o campo `detalhes` traz a lista de campos inválidos:

```json
{
  "timestamp": "2026-05-19T10:30:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "detalhes": [
    { "campo": "dadosPessoais.cpf", "mensagem": "must match \"\\d{11}\"" }
  ]
}
```

### Tabela de códigos HTTP

| HTTP | Cenário |
|---|---|
| 200 | OK (GET, PUT, PATCH com sucesso) |
| 201 | Criado (POST com sucesso — retorna `Location` header) |
| 204 | Sem conteúdo (DELETE com sucesso) |
| 400 | Bad Request — validação de body (`@Valid`) |
| 404 | Not Found — `EntidadeNaoEncontradaException` |
| 409 | Conflict — `RegraNegocioException` (ex: CPF/CNPJ/VIN duplicado, regras de negócio violadas) ou violação de integridade |
| 500 | Internal Server Error — fallback genérico |

## Banco de dados

### Conexão

- **JDBC URL:** `jdbc:mysql://localhost:3306/vinsight?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Sao_Paulo`
- **Schema:** `vinsight` (criado automaticamente)
- **Dialect:** `org.hibernate.dialect.MySQLDialect`
- **ddl-auto:** `validate` — Hibernate apenas valida o schema; o Flyway é quem cria

### Migrations

Flyway controla o schema versionado. Migrations em `src/main/resources/db/migration/`:

| Versão | Arquivo | Descrição |
|---|---|---|
| V1 | `V1__create_concessionaria_table.sql` | Tabela `concessionarias` |
| V2 | `V2__create_cliente_table.sql` | Tabela `clientes` |
| V3 | `V3__create_veiculo_table.sql` | Tabela `veiculos` + FKs para `clientes` e `concessionarias` |
| V4 | `V4__create_agendamento_table.sql` | Tabela `agendamentos` + FKs para `veiculos` e `concessionarias` |
| V5 | `V5__create_lead_table.sql` | Tabela `leads` + FKs para `clientes` e `veiculos` |

> **Regra:** nunca editar uma migration já aplicada. Sempre criar uma nova `V{n+1}__nome.sql` com `ALTER TABLE` se for ajustar schema existente.

## Decisões de projeto

- **MySQL no lugar de H2** — banco real local em vez de in-memory, para validar com o setup que vai pra produção.
- **Embeddables como classes Lombok** (não records) — JPA 3.x não suporta records como `@Embeddable`. Records ficam só nos DTOs.
- **Soft delete em todas as features** — `ativo=false` em Cliente/Concessionária, mudança de `status` em Veículo/Agendamento. Histórico nunca é perdido.
- **FKs `LAZY` + métodos de service `@Transactional`** — evita N+1 nas listagens; DTO constructors acessam FKs dentro da transação.
- **`@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`** — envelope de paginação estável (não expõe campos internos de `PageImpl`).
- **DTOs aninhados (`ClienteResumo`, `VeiculoResumo`, ...)** — em responses detalhe, evita JSON gigante e ciclos.

Challenge FIAP 2026 — Ford Motor Company — Grupo 02.
