package br.com.fiap.vinsight_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Documentacao interativa (US-32): Swagger UI em /swagger-ui.html, especificacao em /api-docs.
 *
 * Aqui ficam o que vale para a API inteira: descricao de abertura, ordem dos grupos (um por
 * servico de dominio), esquema de autenticacao "bearer-key" (botao Authorize) e o schema
 * "Problema" de todos os erros. Os codigos de resposta de cada endpoint vem de DocumentacaoRespostas.
 */
@Configuration
public class SwaggerConfig {

    private static final String DESCRICAO = """
            Backend SOA da plataforma **VINSight Ford**: fila de clientes priorizada pelo risco de evasão, \
            visão 360° do cliente, passaporte do veículo e registro do desfecho de cada contato.

            ### Como autenticar
            1. Em **Autenticação → POST /api/v1/auth/login**, clique em *Try it out* e envie o exemplo \
            (`consultor@ford.com.br` / `consultor123`).
            2. Copie o `accessToken` da resposta.
            3. Clique em **Authorize** (cadeado), cole o token e confirme. Ele vale 15 minutos.

            | Usuário | Senha | Perfil | Unidade |
            |---|---|---|---|
            | consultor@ford.com.br | consultor123 | CONSULTOR | Ford Morumbi |
            | consultor.campinas@ford.com.br | consultor123 | CONSULTOR | Ford Campinas |
            | gerente@ford.com.br | gerente123 | GERENTE | Ford Morumbi |
            | analista@ford.com.br | analista123 | ANALISTA_FORD | rede inteira |
            | admin@ford.com.br | admin123 | ADMIN | rede inteira |

            ### Erros
            Todo erro sai em **Problem Details (RFC 7807)**, com `Content-Type: application/problem+json`. \
            Trate o erro pelo campo `type` (ex.: `https://vinsight.ford/errors/token-expirado`), nunca pelo texto. \
            Cada endpoint lista abaixo os erros que pode responder, com um exemplo de cada.
            """;

    // Um grupo por servico de dominio, na ordem do fluxo do app: entrar, trabalhar a fila, abrir
    // cliente e veiculo, agendar. Os nomes precisam ser iguais aos @Tag dos controllers.
    private static final List<Tag> GRUPOS = List.of(
            new Tag().name("Autenticação").description("Login e renovação do token JWT. Endpoints públicos."),
            new Tag().name("Leads (Lead Engine)").description("Fila de trabalho do consultor e registro do desfecho de cada contato."),
            new Tag().name("Clientes (Customer Service)").description("Busca e visão 360° do cliente, filtradas pela carteira da unidade."),
            new Tag().name("Veículos (Vehicle Service)").description("Passaporte do veículo por VIN: garantia, revisão, histórico e telemetria."),
            new Tag().name("Agendamentos").description("Agenda de serviços da concessionária do usuário."),
            new Tag().name("Concessionárias").description("Unidades da rede Ford. Leitura para todos os perfis; escrita só do ADMIN."),
            new Tag().name("Actuator").description("Health check público, usado pelo app e por monitoramento."));

    @Bean
    public OpenAPI vinsightOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VINSight Ford API")
                        .version("Sprint 3")
                        .description(DESCRICAO)
                        .contact(new Contact().name("Grupo 02 · Glauco Heitor Gonçalves e Pedro Henrique Junqueira")))
                .tags(new ArrayList<>(GRUPOS))
                .components(new Components()
                        .addSecuritySchemes("bearer-key", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token devolvido por POST /api/v1/auth/login (válido por 15 minutos)."))
                        .addSchemas("Violacao", new ObjectSchema()
                                .description("Campo inválido e o motivo")
                                .addProperty("campo", new StringSchema().example("desfecho"))
                                .addProperty("mensagem", new StringSchema().example("não deve ser nulo")))
                        .addSchemas("Problema", problema()));
    }

    // Os controllers tambem declaram @Tag; o springdoc junta as duas listas, repete os grupos e poe
    // os dos controllers na frente. Aqui ficam os GRUPOS, nesta ordem, e depois qualquer outro.
    @Bean
    public OpenApiCustomizer gruposNaOrdemDoFluxo() {
        return openApi -> {
            if (openApi.getTags() == null) return;
            Map<String, Tag> porNome = new LinkedHashMap<>();
            GRUPOS.forEach(tag -> porNome.put(tag.getName(), tag));
            openApi.getTags().forEach(tag -> porNome.putIfAbsent(tag.getName(), tag));
            openApi.setTags(new ArrayList<>(porNome.values()));
        };
    }

    // Formato de todo erro da API (ver GlobalExceptionHandler)
    private static Schema<?> problema() {
        return new ObjectSchema()
                .description("Erro no formato Problem Details (RFC 7807)")
                .addProperty("type", new StringSchema().format("uri")
                        .description("Identifica a situação; o app trata o erro por este campo")
                        .example("https://vinsight.ford/errors/validacao"))
                .addProperty("title", new StringSchema().example("Erro de validação"))
                .addProperty("status", new IntegerSchema().example(422))
                .addProperty("detail", new StringSchema().description("Explicação em português, pronta para a tela")
                        .example("Um ou mais campos estão inválidos."))
                .addProperty("instance", new StringSchema().description("Rota que gerou o erro").example("/api/v1/leads/12"))
                .addProperty("timestamp", new DateTimeSchema().example("2026-09-22T14:30:00Z"))
                .addProperty("correlationId", new StringSchema()
                        .description("Mesmo valor do header X-Correlation-Id e da linha do log do servidor")
                        .example("b7f3c2a1-5d4e-4f6a-9b8c-1d2e3f4a5b6c"))
                .addProperty("violacoes", new ArraySchema()
                        .description("Só nos erros de validação (422)")
                        .items(new Schema<>().$ref("#/components/schemas/Violacao")));
    }
}
