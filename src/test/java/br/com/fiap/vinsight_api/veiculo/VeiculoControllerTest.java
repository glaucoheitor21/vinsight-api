package br.com.fiap.vinsight_api.veiculo;

import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-34 (Vehicle Service): passaporte por VIN, com status derivados calculados sobre o
 * "hoje" fixo de 22/09/2026 (RelogioFixoConfig).
 */
class VeiculoControllerTest extends TesteIntegracao {

    private static final String RANGER_CARLOS = "9BF8313PF9WBWDX01";   // Morumbi
    private static final String MAVERICK_MARIANA = "9BFXR3MJLVF4SB8T9"; // Campinas
    private static final String MACHE_CAMILA = "9BFC05GG9JY36J9B5";     // Morumbi
    private static final String TERRITORY_JOAO = "9BFJKD4NL04BHG2Z4";   // dono nas duas carteiras

    // Espiao sobre o service real: permite provar que o VIN invalido nem chega ate ele
    @MockitoSpyBean
    private VeiculoService veiculoService;

    @Nested
    @DisplayName("BDD: consulta por VIN retorna o passaporte completo")
    class Passaporte {

        @Test
        @DisplayName("Dados do veículo, garantia, revisão, aderência, telemetria e histórico em ordem decrescente")
        void passaporteCompleto() throws Exception {
            mvc.perform(get("/api/v1/vehicles/" + RANGER_CARLOS).header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.modelo").value("Ranger"))
                    .andExpect(jsonPath("$.ano").value(2023))
                    // Garantia vence em 15/11/2026: 54 dias -> PROXIMA_DO_FIM
                    .andExpect(jsonPath("$.garantia.status").value("PROXIMA_DO_FIM"))
                    .andExpect(jsonPath("$.garantia.dataLimite").value("2026-11-15"))
                    .andExpect(jsonPath("$.garantia.mesesRestantes").value(1))
                    // 81.000 km >= 80.000 km previstos -> VENCIDA
                    .andExpect(jsonPath("$.proximaRevisaoPrevista.situacao").value("VENCIDA"))
                    // 3 ordens na rede de 4 -> 0,75
                    .andExpect(jsonPath("$.aderenciaRede").value(0.75))
                    .andExpect(jsonPath("$.ultimaTelemetria.recebidaEm").value("2026-09-16T01:10:00Z"))
                    .andExpect(jsonPath("$.ultimaTelemetria.codigosFalha", contains("P0301", "P0171")))
                    .andExpect(jsonPath("$.historico[*].data", contains("2026-06-10", "2026-01-10", "2025-09-10", "2025-03-10")))
                    .andExpect(jsonPath("$.historico[0].naRede").value(false))
                    .andExpect(jsonPath("$.historico[0].concessionaria").value(nullValue()))
                    .andExpect(jsonPath("$.historico[1].concessionaria").value("Ford Morumbi"));

            // Controle do espiao: com VIN valido o service E chamado. Sem isto, o never() dos testes
            // de VIN invalido passaria mesmo se o espiao nao funcionasse.
            verify(veiculoService).passaporte(RANGER_CARLOS);
        }

        @Test
        @DisplayName("Garantia ATIVA, revisão EM_DIA e sem histórico nem telemetria")
        void veiculoNovo() throws Exception {
            mvc.perform(get("/api/v1/vehicles/" + MAVERICK_MARIANA).header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(jsonPath("$.garantia.status").value("ATIVA"))
                    .andExpect(jsonPath("$.proximaRevisaoPrevista.situacao").value("EM_DIA"))
                    .andExpect(jsonPath("$.aderenciaRede").value(nullValue()))
                    .andExpect(jsonPath("$.ultimaTelemetria").value(nullValue()));
        }

        @Test
        @DisplayName("Garantia ENCERRADA e revisão PROXIMA pela data (18 dias)")
        void garantiaEncerrada() throws Exception {
            mvc.perform(get("/api/v1/vehicles/" + MACHE_CAMILA).header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(jsonPath("$.garantia.status").value("ENCERRADA"))
                    .andExpect(jsonPath("$.garantia.mesesRestantes").value(0))
                    .andExpect(jsonPath("$.proximaRevisaoPrevista.situacao").value("PROXIMA"));
        }

        @Test
        @DisplayName("VIN válido que não existe responde 404")
        void vinInexistente() throws Exception {
            mvc.perform(get("/api/v1/vehicles/9BFZZZZZZZZZZZZZZ").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value(ERRORS + "nao-encontrado"));
        }
    }

    @Nested
    @DisplayName("BDD: VIN em formato inválido é rejeitado antes da consulta")
    class VinInvalido {

        @Test
        @DisplayName("VIN com 16 caracteres: 422 no campo vin, e o service (logo, o banco) nunca é chamado")
        void vinCurto() throws Exception {
            mvc.perform(get("/api/v1/vehicles/9BF8313PF9WBWDX0").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.type").value(ERRORS + "validacao"))
                    .andExpect(jsonPath("$.violacoes[0].campo").value("vin"));

            verify(veiculoService, never()).passaporte(anyString());
        }

        @Test
        @DisplayName("VIN com a letra O (proibida, confunde com zero) também é 422")
        void vinComLetraProibida() throws Exception {
            mvc.perform(get("/api/v1/vehicles/9BF8313PF9WBWDXO1").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isUnprocessableContent());

            verify(veiculoService, never()).passaporte(anyString());
        }
    }

    @Nested
    @DisplayName("Escopo: carteira do dono do veículo")
    class Escopo {

        @Test
        @DisplayName("Veículo de dono fora da carteira responde 403 outra-concessionaria")
        void donoForaDaCarteira() throws Exception {
            mvc.perform(get("/api/v1/vehicles/" + RANGER_CARLOS).header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.type").value(ERRORS + "outra-concessionaria"));
        }

        @Test
        @DisplayName("Dono presente nas duas carteiras: as duas unidades veem o veículo")
        void donoEmDuasCarteiras() throws Exception {
            mvc.perform(get("/api/v1/vehicles/" + TERRITORY_JOAO).header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/vehicles/" + TERRITORY_JOAO).header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Listagem traz só os veículos da carteira")
        void listagemPorCarteira() throws Exception {
            mvc.perform(get("/api/v1/vehicles").header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(jsonPath("$.content[*].vin", containsInAnyOrder(MAVERICK_MARIANA, TERRITORY_JOAO)));
        }

        @Test
        @DisplayName("Busca por placa aceita hífen e minúsculas")
        void buscaPorPlaca() throws Exception {
            mvc.perform(get("/api/v1/vehicles?placa=wcd-1z37").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].vin").value(RANGER_CARLOS));
        }

        @Test
        @DisplayName("Não é possível cadastrar veículo para cliente fora da carteira")
        void cadastroParaClienteDeOutraUnidade() throws Exception {
            mvc.perform(post("/api/v1/vehicles")
                            .header("Authorization", bearer(CONSULTOR_CAMPINAS))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"vin":"9BFTESTE000000001","placa":"TST1A23","modelo":"Ka",
                                     "anoFabricacao":2020,"anoModelo":2020,"clienteId":1}"""))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Cadastro válido: 201, Location com o VIN e CPF do dono mascarado")
        void cadastroValido() throws Exception {
            mvc.perform(post("/api/v1/vehicles")
                            .header("Authorization", bearer(CONSULTOR_MORUMBI))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"vin":"9BFTESTE000000001","placa":"TST1A23","modelo":"Ka",
                                     "anoFabricacao":2020,"anoModelo":2020,"clienteId":1}"""))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "http://localhost/api/v1/vehicles/9BFTESTE000000001"))
                    .andExpect(jsonPath("$.cliente.cpf").value("***.222.334-**"));
        }

        @Test
        @DisplayName("Consultor não inativa veículo (só GERENTE e ADMIN)")
        void consultorNaoInativa() throws Exception {
            mvc.perform(delete("/api/v1/vehicles/" + RANGER_CARLOS).header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isForbidden());
        }
    }
}
