package br.com.fiap.vinsight_api.concessionaria;

import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Concessionarias: leitura para todos os perfis, escrita so do ADMIN (matriz da US-30). */
class ConcessionariaControllerTest extends TesteIntegracao {

    private static final String NOVA = """
            {"nomeFantasia":"Ford Teste","razaoSocial":"Teste SA","cnpj":"12345678000199",
             "endereco":{"logradouro":"Rua A","numero":"1","bairro":"B","cidade":"C","uf":"SP","cep":"01001000"},
             "contato":{"email":"teste@ford.com.br","telefone":"11999999999"}}""";

    @Test
    @DisplayName("Todos os perfis leem concessionárias, inclusive o analista Ford")
    void leituraLiberada() throws Exception {
        for (String usuario : new String[]{CONSULTOR_MORUMBI, GERENTE_MORUMBI, ANALISTA, ADMIN}) {
            mvc.perform(get("/api/v1/concessionarias").header("Authorization", bearer(usuario)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }
    }

    @Test
    @DisplayName("Analista com corpo válido não cadastra (403): escrita é só do ADMIN")
    void analistaNaoCadastra() throws Exception {
        mvc.perform(post("/api/v1/concessionarias")
                        .header("Authorization", bearer(ANALISTA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value(ERRORS + "perfil-sem-permissao"));
    }

    @Test
    @DisplayName("Consultor não altera concessionária (403)")
    void consultorNaoAltera() throws Exception {
        mvc.perform(put("/api/v1/concessionarias/1")
                        .header("Authorization", bearer(CONSULTOR_MORUMBI))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cadastra: 201 com Location")
    void adminCadastra() throws Exception {
        mvc.perform(post("/api/v1/concessionarias")
                        .header("Authorization", bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVA))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("Admin detalha, altera parcialmente (PUT) e inativa (204); inativada some da lista")
    void cicloDoAdmin() throws Exception {
        mvc.perform(get("/api/v1/concessionarias/1").header("Authorization", bearer(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFantasia").value("Ford Morumbi"));

        mvc.perform(put("/api/v1/concessionarias/1")
                        .header("Authorization", bearer(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeFantasia\":\"Ford Morumbi Premium\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFantasia").value("Ford Morumbi Premium"))
                .andExpect(jsonPath("$.razaoSocial").value("Morumbi Veiculos Ltda"));

        mvc.perform(delete("/api/v1/concessionarias/2").header("Authorization", bearer(ADMIN)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/concessionarias").header("Authorization", bearer(ADMIN)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Concessionária inexistente responde 404")
    void inexistente() throws Exception {
        mvc.perform(get("/api/v1/concessionarias/999").header("Authorization", bearer(ADMIN)))
                .andExpect(status().isNotFound());
    }
}
