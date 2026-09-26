package br.com.fiap.vinsight_api.config;

import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-32: a documentacao OpenAPI reflete a API real.
 *
 * Le a especificacao gerada em /api-docs e confere, para TODOS os endpoints, os codigos de
 * resposta que a API de fato devolve. Um endpoint novo mal documentado quebra este teste.
 *
 * Tambem compara com docs/openapi.json, a copia versionada da especificacao. Se um endpoint for
 * criado ou removido, o teste falha ate a copia ser atualizada:
 *   ./mvnw test -Dtest=DocumentacaoOpenApiTest -Dopenapi.atualizar=true
 */
class DocumentacaoOpenApiTest extends TesteIntegracao {

    private static final Path ESPECIFICACAO_VERSIONADA = Path.of("docs", "openapi.json");
    private static final String PROBLEMA = "#/components/schemas/Problema";

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode especificacao;

    private record Operacao(String metodo, String rota, JsonNode corpo) {
        String chave() {
            return metodo.toUpperCase() + " " + rota;
        }

        boolean documenta(String status) {
            return corpo.path("responses").has(status);
        }
    }

    @BeforeEach
    void lerEspecificacao() throws Exception {
        String json = mvc.perform(get("/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        especificacao = objectMapper.readTree(json);
    }

    private List<Operacao> operacoesDaApi() {
        List<Operacao> operacoes = new ArrayList<>();
        for (Map.Entry<String, JsonNode> rota : especificacao.path("paths").properties()) {
            if (!rota.getKey().startsWith("/api/v1/")) continue;
            for (Map.Entry<String, JsonNode> metodo : rota.getValue().properties()) {
                operacoes.add(new Operacao(metodo.getKey(), rota.getKey(), metodo.getValue()));
            }
        }
        return operacoes;
    }

    private static boolean publica(Operacao op) {
        return op.rota().startsWith("/api/v1/auth/");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "POST /api/v1/auth/login", "POST /api/v1/auth/refresh",
            "GET /api/v1/leads", "PATCH /api/v1/leads/{id}",
            "GET /api/v1/customers", "GET /api/v1/customers/{id}/overview",
            "GET /api/v1/vehicles/{vin}", "GET /actuator/health"
    })
    @DisplayName("Endpoints do contrato da Sprint 3 estão documentados")
    void endpointsDoContrato(String endpoint) {
        String metodo = endpoint.substring(0, endpoint.indexOf(' ')).toLowerCase();
        String rota = endpoint.substring(endpoint.indexOf(' ') + 1);
        assertThat(especificacao.path("paths").path(rota).has(metodo))
                .as("%s ausente da documentação", endpoint).isTrue();
    }

    @Test
    @DisplayName("Todo endpoint protegido exige o Bearer token e documenta 401")
    void protegidosDocumentam401() {
        List<String> semAutenticacao = operacoesDaApi().stream()
                .filter(op -> !publica(op))
                .filter(op -> !op.corpo().has("security") || !op.documenta("401"))
                .map(Operacao::chave).toList();
        assertThat(semAutenticacao).isEmpty();
    }

    @Test
    @DisplayName("Endpoints públicos (login e refresh) não pedem token")
    void publicosSemToken() {
        operacoesDaApi().stream().filter(DocumentacaoOpenApiTest::publica)
                .forEach(op -> assertThat(op.corpo().has("security")).as(op.chave()).isFalse());
    }

    @Test
    @DisplayName("Toda rota com identificador ({id}, {vin}) documenta 404")
    void identificadorDocumenta404() {
        List<String> sem404 = operacoesDaApi().stream()
                .filter(op -> op.rota().contains("{") && !op.documenta("404"))
                .map(Operacao::chave).toList();
        assertThat(sem404).isEmpty();
    }

    @Test
    @DisplayName("Criação documenta 201 com Location, e remoção documenta 204, como a API responde")
    void statusDeSucesso() {
        for (Operacao op : operacoesDaApi()) {
            if (op.metodo().equals("post") && !publica(op)) {
                assertThat(op.documenta("201")).as(op.chave()).isTrue();
                assertThat(op.corpo().path("responses").path("201").path("headers").has("Location")).as(op.chave()).isTrue();
            }
            if (op.metodo().equals("delete")) {
                assertThat(op.documenta("204")).as(op.chave()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Todo endpoint com corpo documenta 422 (validação) e todos documentam 500")
    void validacaoEErroInterno() {
        for (Operacao op : operacoesDaApi()) {
            assertThat(op.documenta("500")).as(op.chave()).isTrue();
            if (op.corpo().has("requestBody")) {
                assertThat(op.documenta("422")).as(op.chave()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Todo erro documentado usa o schema Problema (RFC 7807) e traz exemplo")
    void errosNoFormatoProblema() {
        for (Operacao op : operacoesDaApi()) {
            for (Map.Entry<String, JsonNode> resposta : op.corpo().path("responses").properties()) {
                if (Integer.parseInt(resposta.getKey()) < 400) continue;
                JsonNode problema = resposta.getValue().path("content").path("application/problem+json");
                assertThat(problema.path("schema").path("$ref").asString()).as("%s %s", op.chave(), resposta.getKey()).isEqualTo(PROBLEMA);
                assertThat(problema.path("examples").isEmpty()).as("%s %s sem exemplo", op.chave(), resposta.getKey()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("Erros de regra de negócio aparecem onde acontecem: 409 no desfecho e no cadastro de cliente")
    void errosDeNegocio() {
        assertThat(especificacao.path("paths").path("/api/v1/leads/{id}").path("patch").path("responses").has("409")).isTrue();
        assertThat(especificacao.path("paths").path("/api/v1/customers").path("post").path("responses").has("409")).isTrue();
        // O agendamento não tem conflito possível hoje: não deve documentar 409
        assertThat(especificacao.path("paths").path("/api/v1/agendamentos").path("post").path("responses").has("409")).isFalse();
    }

    @Test
    @DisplayName("Grupos seguem os serviços de domínio, na ordem do fluxo do app")
    void gruposPorServico() {
        List<String> grupos = new ArrayList<>();
        especificacao.path("tags").forEach(t -> grupos.add(t.path("name").asString()));
        assertThat(grupos).startsWith("Autenticação", "Leads (Lead Engine)", "Clientes (Customer Service)",
                "Veículos (Vehicle Service)", "Agendamentos", "Concessionárias");
        assertThat(new TreeSet<>(grupos)).hasSameSizeAs(grupos);
    }

    @Test
    @DisplayName("BDD: o exemplo do login funciona e o token dele dá 200 com dados reais num endpoint protegido")
    void exemploDoLoginFunciona() throws Exception {
        JsonNode login = especificacao.path("components").path("schemas").path("DadosLogin").path("properties");
        String corpo = objectMapper.writeValueAsString(Map.of(
                "email", login.path("email").path("example").asString(),
                "senha", login.path("senha").path("example").asString()));

        String resposta = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(resposta).path("accessToken").asString();

        mvc.perform(get("/api/v1/leads").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @DisplayName("A especificação versionada em docs/openapi.json está em dia com a API")
    void especificacaoVersionadaEmDia() throws Exception {
        if (Boolean.getBoolean("openapi.atualizar")) {
            // O MockMvc gera "http://localhost"; a copia versionada aponta para a API local de verdade,
            // para quem importar o arquivo no Postman ou Insomnia
            ObjectNode copia = (ObjectNode) especificacao.deepCopy();
            copia.putArray("servers").addObject()
                    .put("url", "http://localhost:8080")
                    .put("description", "API local, perfil dev (./mvnw spring-boot:run)");
            Files.createDirectories(ESPECIFICACAO_VERSIONADA.getParent());
            Files.writeString(ESPECIFICACAO_VERSIONADA,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(copia) + System.lineSeparator());
        }
        assertThat(ESPECIFICACAO_VERSIONADA).as("gere com -Dopenapi.atualizar=true").exists();

        JsonNode versionada = objectMapper.readTree(Files.readString(ESPECIFICACAO_VERSIONADA));
        assertThat(endpoints(versionada))
                .as("docs/openapi.json desatualizado: rode ./mvnw test -Dtest=DocumentacaoOpenApiTest -Dopenapi.atualizar=true")
                .isEqualTo(endpoints(especificacao));
    }

    private static Set<String> endpoints(JsonNode spec) {
        Set<String> endpoints = new TreeSet<>();
        for (Map.Entry<String, JsonNode> rota : spec.path("paths").properties()) {
            for (Map.Entry<String, JsonNode> metodo : rota.getValue().properties()) {
                JsonNode respostas = metodo.getValue().path("responses");
                endpoints.add(metodo.getKey().toUpperCase() + " " + rota.getKey() + " " + new TreeSet<>(respostas.propertyNames()));
            }
        }
        return endpoints;
    }
}
