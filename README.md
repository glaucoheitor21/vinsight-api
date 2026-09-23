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
- Spring Web · Spring Data JPA · Validation · **Actuator**
- **Spring Security 7** + **java-jwt (Auth0) 4.6.0** — autenticação JWT (US-29)
- **MySQL 8** (banco de dados) — inclusive na suíte de testes, em schema dedicado
- **Flyway** (migrations versionadas)
- **Lombok**
- **SpringDoc OpenAPI** (Swagger UI auto-gerado)
- **JaCoCo** (relatório de cobertura)

## Pré-requisitos

- JDK 21
- MySQL 8 rodando localmente (porta padrão `3306`)
- Maven 3.9+ (ou use o `mvnw` que vem no repo)

## Como rodar

### 1. Configurar credenciais do MySQL

O default é `root` / `fiap`. Se o seu MySQL usa outras credenciais, **não edite o código** — passe por
variável de ambiente:

```bash
DB_USER=seu_usuario DB_PASSWORD=sua_senha ./mvnw spring-boot:run
```

Variáveis reconhecidas: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`.

A URL JDBC já tem `createDatabaseIfNotExist=true`, então o schema `vinsight` é criado automaticamente na primeira execução — não precisa rodar `CREATE DATABASE` manualmente.

> Se aparecer `Public Key Retrieval is not allowed`, o MySQL 8 está usando `caching_sha2_password`.
> A URL de dev já inclui `allowPublicKeyRetrieval=true` para resolver isso.

### 2. Subir a aplicação

```bash
./mvnw spring-boot:run
```

ou, no Windows:

```cmd
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. Flyway aplica todas as migrations em
`src/main/resources/db/migration/` na ordem (V1 → V12) e, no perfil `dev`, também a massa de
demonstração em `src/main/resources/db/seed/`.

### 3. Conferir que subiu

```bash
curl http://localhost:8080/actuator/health
```

Deve responder `200` com `"status":"UP"`. Em seguida abra o Swagger em
http://localhost:8080/swagger-ui.html.

## Perfis de execução

| Perfil | Banco | Massa de demonstração | Uso |
|---|---|---|---|
| `dev` (**default**) | MySQL local | sim (`db/seed`) | desenvolvimento e demonstração |
| `prod` | MySQL via `DB_URL` | não | tudo por variável de ambiente, sem credencial no repo |
| `test` | MySQL, schema `vinsight_test` | não | suíte automatizada — nunca toca o banco `vinsight` |

```bash
SPRING_PROFILES_ACTIVE=prod DB_URL=... DB_USER=... DB_PASSWORD=... ./mvnw spring-boot:run
```

## Usuários de demonstração

Criados pelo seed do perfil `dev` (senhas em BCrypt no banco). Serão utilizáveis a partir da US-29,
quando o endpoint de login existir.

| E-mail | Senha | Perfil | Concessionária |
|---|---|---|---|
| `consultor@ford.com.br` | `consultor123` | CONSULTOR | Ford Morumbi (SP-001) |
| `consultor.campinas@ford.com.br` | `consultor123` | CONSULTOR | Ford Campinas (SP-014) |
| `gerente@ford.com.br` | `gerente123` | GERENTE | Ford Morumbi (SP-001) |
| `analista@ford.com.br` | `analista123` | ANALISTA_FORD | — (rede inteira) |
| `admin@ford.com.br` | `admin123` | ADMIN | — |

Os dois consultores em unidades diferentes existem de propósito: são eles que provam o escopo de
dados por concessionária da US-30.

## Executar os testes

```bash
./mvnw verify
```

Roda no perfil `test`, que usa um **schema separado** `vinsight_test` no mesmo MySQL local. Ele é
criado sozinho no primeiro `verify` e recebe apenas as migrations `V1→V12` — sem a massa de
demonstração. Assim os testes nunca escrevem no banco `vinsight` usado na apresentação, e nunca
dependem do que o seed deixou lá.

Como o perfil usa `spring.jpa.hibernate.ddl-auto=validate`, o build também falha se uma entidade
divergir do schema criado pelas migrations.

Para zerar o banco de teste: `DROP DATABASE vinsight_test;` — ele é recriado na execução seguinte.

O relatório JaCoCo fica em `target/site/jacoco/index.html`.

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

### Clientes (Customer Service — US-33)

Filtrados pela **carteira da unidade** do usuário: um cliente aparece para toda concessionária com
que tem vínculo (cadastro, compra, serviço ou agendamento). CPF, telefone e e-mail saem
**mascarados para o CONSULTOR**; GERENTE e ADMIN veem os dados completos.

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/customers` | Cadastra novo cliente (entra na carteira de quem cadastrou) |
| GET | `/customers?q=` | Busca por nome parcial, CPF ou telefone (paginado); sem `q`, lista a carteira |
| GET | `/customers/{id}` | Detalha o cadastro |
| GET | `/customers/{id}/overview` | Visão 360°: cadastro, veículos, consentimento, NPS e resumo do histórico |
| GET | `/customers/{id}/vehicles` | Lista veículos do cliente |
| PUT | `/customers/{id}` | Atualiza cliente |
| DELETE | `/customers/{id}` | Inativa cliente (soft delete) — GERENTE e ADMIN |

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
| V6 | `V6__create_usuario_table.sql` | Tabela `usuarios` (perfil + concessionária) — base da US-29/30 |
| V7 | `V7__create_ordem_servico_table.sql` | Tabela `ordens_servico` — histórico e insumo do Service Share |
| V8 | `V8__alter_lead_add_concessionaria_e_campos_contrato.sql` | `concessionaria_id`, `motivo_contato`, `acao_recomendada`, `perfil_comportamental` |
| V9 | `V9__alter_cliente_add_consentimento_e_nps.sql` | Consentimento LGPD, canal preferido e NPS |
| V10 | `V10__alter_veiculo_add_garantia_e_telemetria.sql` | Cor, quilometragem, garantia, próxima revisão, telemetria |
| V11 | `V11__create_requisicao_idempotente_table.sql` | Suporte ao header `Idempotency-Key` |
| V12 | `V12__alter_concessionaria_add_codigo.sql` | Código da unidade (ex.: `SP-001`) |
| V13 | `V13__alter_cliente_add_concessionaria_cadastro.sql` | Unidade que cadastrou o cliente (carteira da US-33) |

Massa de demonstração em `src/main/resources/db/seed/V900__seed_demo_data.sql`, carregada **apenas no
perfil `dev`** (o `prod` não inclui `db/seed` em `spring.flyway.locations`). O arquivo é gerado, não
editado à mão: 3 concessionárias, 8 usuários, 30 clientes, 37 veículos, 148 ordens de serviço,
19 agendamentos e 33 leads.

Correções e complementos da massa ficam em `db/seed/V9xx`, numerados depois da V900, para rodar
depois dela tanto num banco novo quanto num existente: `V901` (vocabulário de `tipoServico`) e `V902`
(cliente atendido em duas unidades, que demonstra a carteira por relacionamento).

Para regerar: `python tools/gen_seed.py`. O script usa semente fixa, então a saída é sempre idêntica.
Se regerar **depois** de o seed já ter sido aplicado, o checksum muda e o Flyway aborta — nesse caso,
`DROP DATABASE vinsight;` e suba de novo.

> **Regra:** nunca editar uma migration já aplicada. Sempre criar uma nova `V{n+1}__nome.sql` com `ALTER TABLE` se for ajustar schema existente.

## Decisões de projeto

- **MySQL em todos os perfis, sem H2** — banco real local, o mesmo ecossistema usado na faculdade. O isolamento dos testes vem de um **schema separado** (`vinsight_test`), não de um banco in-memory: os testes exercitam as migrations e o dialeto de verdade.
- **Seed separado das migrations** — `db/migration` é schema, `db/seed` é dado de demonstração. Só o perfil `dev` lê o segundo.
- **Embeddables como classes Lombok** (não records) — JPA 3.x não suporta records como `@Embeddable`. Records ficam só nos DTOs.
- **Soft delete em todas as features** — `ativo=false` em Cliente/Concessionária, mudança de `status` em Veículo/Agendamento. Histórico nunca é perdido.
- **FKs `LAZY` + métodos de service `@Transactional`** — evita N+1 nas listagens; DTO constructors acessam FKs dentro da transação.
- **`@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`** — envelope de paginação estável (não expõe campos internos de `PageImpl`).
- **DTOs aninhados (`ClienteResumo`, `VeiculoResumo`, ...)** — em responses detalhe, evita JSON gigante e ciclos.

Challenge FIAP 2026 — Ford Motor Company — Grupo 02.
