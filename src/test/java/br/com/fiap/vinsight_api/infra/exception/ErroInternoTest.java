package br.com.fiap.vinsight_api.infra.exception;

import br.com.fiap.vinsight_api.concessionaria.ConcessionariaService;
import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BDD da US-31: "erro inesperado não vaza detalhes internos". Nenhum endpoint real quebra de
 * proposito, entao o service e trocado por um mock do Mockito que lanca uma excecao com dados
 * "sensiveis" na mensagem. Fica numa classe propria porque o @MockitoBean pede outro contexto.
 */
class ErroInternoTest extends TesteIntegracao {

    @MockitoBean
    private ConcessionariaService concessionariaService;

    @Test
    @DisplayName("500 com mensagem genérica e correlationId, sem stack trace, nome de classe ou detalhe interno")
    void erroInternoNaoVaza() throws Exception {
        when(concessionariaService.listar(any()))
                .thenThrow(new IllegalStateException("falha em br.com.fiap.vinsight_api.X: senha=segredo123"));

        mvc.perform(get("/api/v1/concessionarias").header("Authorization", bearer(ADMIN)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value(ERRORS + "erro-interno"))
                .andExpect(jsonPath("$.detail").value("Erro inesperado. Informe o correlationId ao suporte."))
                .andExpect(jsonPath("$.correlationId").isString())
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("br.com.fiap"))))
                .andExpect(content().string(not(containsString("segredo123"))))
                .andExpect(content().string(not(containsString("at "))));
    }
}
