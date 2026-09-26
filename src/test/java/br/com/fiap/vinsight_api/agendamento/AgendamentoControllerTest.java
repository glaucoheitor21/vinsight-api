package br.com.fiap.vinsight_api.agendamento;

import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Agendamentos: escopo por unidade (US-30), o filtro ?concessionariaId que nao pode furar o
 * escopo, e o vocabulario de tipoServico do contrato (US-31, divergencia 5.2).
 * Massa: Morumbi tem o agendamento 1; Campinas, o 2 e o 3.
 */
class AgendamentoControllerTest extends TesteIntegracao {

    // Data longe no futuro: o @Future do Bean Validation usa o relogio real, nao o RelogioFixoConfig
    private static final String AGENDAMENTO_MORUMBI = """
            {"veiculoId":1,"concessionariaId":1,"dataHora":"2030-12-01T10:00:00","tipoServico":"REVISAO_PROGRAMADA"}""";

    @Test
    @DisplayName("Consultor lista só os agendamentos da própria unidade; admin, os da rede")
    void listagemPorUnidade() throws Exception {
        mvc.perform(get("/api/v1/agendamentos").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(jsonPath("$.content[*].id", contains(1)));
        mvc.perform(get("/api/v1/agendamentos").header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/agendamentos").header("Authorization", bearer(ADMIN)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("Filtro ?concessionariaId de outra unidade não fura o escopo do consultor (403)")
    void filtroNaoFuraEscopo() throws Exception {
        mvc.perform(get("/api/v1/agendamentos?concessionariaId=2").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value(ERRORS + "outra-concessionaria"));
    }

    @Test
    @DisplayName("Para o admin, ?concessionariaId é um filtro comum")
    void filtroDoAdmin() throws Exception {
        mvc.perform(get("/api/v1/agendamentos?concessionariaId=2").header("Authorization", bearer(ADMIN)))
                .andExpect(jsonPath("$.content[*].id", contains(2, 3)));
    }

    @Test
    @DisplayName("Ler ou cancelar agendamento de outra unidade responde 403")
    void agendamentoDeOutraUnidade() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/2").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/agendamentos/2").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Criação na própria unidade: 201 com Location")
    void criacaoValida() throws Exception {
        mvc.perform(post("/api/v1/agendamentos")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_MORUMBI))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/api/v1/agendamentos/")))
                .andExpect(jsonPath("$.tipoServico").value("REVISAO_PROGRAMADA"))
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    @DisplayName("Consultor não cria agendamento em outra unidade (403)")
    void criacaoEmOutraUnidade() throws Exception {
        mvc.perform(post("/api/v1/agendamentos")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_MORUMBI.replace("\"concessionariaId\":1", "\"concessionariaId\":2")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Consultor agenda veículo vendido por outra unidade na sua própria (ganho de Service Share)")
    void veiculoDeOutraUnidadeNaPropria() throws Exception {
        mvc.perform(post("/api/v1/agendamentos")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_MORUMBI.replace("\"veiculoId\":1", "\"veiculoId\":2")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("tipoServico da v1 (REVISAO) responde 422 listando os valores aceitos")
    void vocabularioAntigo() throws Exception {
        mvc.perform(post("/api/v1/agendamentos")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_MORUMBI.replace("REVISAO_PROGRAMADA", "REVISAO")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].campo").value("tipoServico"))
                .andExpect(jsonPath("$.violacoes[0].mensagem", containsString("REVISAO_PROGRAMADA")));
    }

    @Test
    @DisplayName("Data no passado responde 422")
    void dataNoPassado() throws Exception {
        mvc.perform(post("/api/v1/agendamentos")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENDAMENTO_MORUMBI.replace("2030-12-01", "2020-01-01")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0].campo").value("dataHora"));
    }

    @Test
    @DisplayName("Agenda de outra unidade por /concessionarias/{id}/agendamentos responde 403")
    void agendaDeOutraConcessionaria() throws Exception {
        mvc.perform(get("/api/v1/concessionarias/2/agendamentos").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/concessionarias/1/agendamentos").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Ciclo na própria unidade: detalhar, confirmar (PATCH parcial) e cancelar (204)")
    void cicloDoAgendamento() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/1").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGENDADO"));

        mvc.perform(patch("/api/v1/agendamentos/1/status")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        mvc.perform(delete("/api/v1/agendamentos/1").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/agendamentos/1").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("Agendamento inexistente responde 404")
    void agendamentoInexistente() throws Exception {
        mvc.perform(get("/api/v1/agendamentos/999").header("Authorization", bearer(ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Analista Ford não acessa agendamentos")
    void analistaSemAcesso() throws Exception {
        mvc.perform(get("/api/v1/agendamentos").header("Authorization", bearer(ANALISTA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value(ERRORS + "perfil-sem-permissao"));
    }
}
