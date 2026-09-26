package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-33 (Customer Service): visao 360, busca, mascaramento e carteira por relacionamento.
 * Carteiras da massa: Morumbi = Carlos, Camila, Joao · Campinas = Mariana, Joao (servico la).
 */
class ClienteControllerTest extends TesteIntegracao {

    private static final String NOVO_CLIENTE = """
            {"dadosPessoais":{"nome":"Walk-in Teste","cpf":"98765432100","dataNascimento":"1990-01-01"},
             "endereco":{"logradouro":"Rua X","numero":"1","bairro":"Centro","cidade":"Campinas","uf":"SP","cep":"13010000"},
             "contato":{"email":"walkin@email.com","telefone":"19999998888"}}""";

    @Nested
    @DisplayName("BDD: visão 360° agregada em uma chamada")
    class Visao360 {

        @Test
        @DisplayName("Overview traz cadastro, veículos, consentimento, canal preferido, NPS e resumo do histórico")
        void overviewCompleto() throws Exception {
            mvc.perform(get("/api/v1/customers/1/overview").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Carlos Pereira"))
                    .andExpect(jsonPath("$.canalPreferido").value("WHATSAPP"))
                    .andExpect(jsonPath("$.consentimento.ativo").value(true))
                    .andExpect(jsonPath("$.consentimento.canais", containsInAnyOrder("WHATSAPP", "EMAIL")))
                    .andExpect(jsonPath("$.consentimento.atualizadoEm").value("2026-03-11T17:22:00Z"))
                    .andExpect(jsonPath("$.ultimoNps").value(9))
                    .andExpect(jsonPath("$.veiculos", hasSize(1)))
                    .andExpect(jsonPath("$.veiculos[0].vin").value("9BF8313PF9WBWDX01"))
                    // 4 ordens (3 na rede + 1 fora): (900 + 600 + 500 + 400) / 4 = 600,00
                    .andExpect(jsonPath("$.resumoHistorico.totalOrdens").value(4))
                    .andExpect(jsonPath("$.resumoHistorico.ticketMedio").value(600.00))
                    .andExpect(jsonPath("$.resumoHistorico.ultimaVisita").value("2026-06-10"));
        }

        @Test
        @DisplayName("Cliente inexistente responde 404 no formato Problem Details")
        void clienteInexistente() throws Exception {
            mvc.perform(get("/api/v1/customers/999/overview").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(ERRORS + "nao-encontrado"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/customers/999/overview"));
        }
    }

    @Nested
    @DisplayName("BDD: mascaramento de dados pessoais por perfil")
    class Mascaramento {

        @Test
        @DisplayName("Consultor recebe documento, telefone e e-mail parcialmente mascarados")
        void consultorRecebeMascarado() throws Exception {
            mvc.perform(get("/api/v1/customers/1/overview").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(jsonPath("$.documentoMascarado").value("***.222.334-**"))
                    .andExpect(jsonPath("$.telefoneMascarado").value("(11) *****-4321"))
                    .andExpect(jsonPath("$.emailMascarado").value("c****@email.com"));
        }

        @Test
        @DisplayName("Gerente recebe os dados completos, nas mesmas chaves do contrato")
        void gerenteRecebeCompleto() throws Exception {
            mvc.perform(get("/api/v1/customers/1/overview").header("Authorization", bearer(GERENTE_MORUMBI)))
                    .andExpect(jsonPath("$.documentoMascarado").value("11122233437"))
                    .andExpect(jsonPath("$.telefoneMascarado").value("11910004321"))
                    .andExpect(jsonPath("$.emailMascarado").value("carlos@email.com"));
        }

        @Test
        @DisplayName("PUT com e-mail mascarado é recusado (422), para não sobrescrever o dado real")
        void naoGravaDadoMascarado() throws Exception {
            mvc.perform(put("/api/v1/customers/1")
                            .header("Authorization", bearer(CONSULTOR_MORUMBI))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contato":{"email":"c****@email.com","telefone":"11910004321"}}"""))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.violacoes[0].campo").value("contato.email"));
        }
    }

    @Nested
    @DisplayName("Carteira por relacionamento (escopo da US-30 aplicado a clientes)")
    class Carteira {

        @Test
        @DisplayName("Cada consultor lista só a carteira da sua unidade; admin vê a rede")
        void listagemPorCarteira() throws Exception {
            mvc.perform(get("/api/v1/customers").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(jsonPath("$.content[*].nome", containsInAnyOrder("Carlos Pereira", "Camila Freitas", "Joao Batista")));
            mvc.perform(get("/api/v1/customers").header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(jsonPath("$.content[*].nome", containsInAnyOrder("Mariana Rocha", "Joao Batista")));
            mvc.perform(get("/api/v1/customers").header("Authorization", bearer(ADMIN)))
                    .andExpect(jsonPath("$.totalElements").value(4));
        }

        @Test
        @DisplayName("Cliente que comprou numa unidade e fez serviço em outra aparece para as duas")
        void clienteEmDuasCarteiras() throws Exception {
            mvc.perform(get("/api/v1/customers/4/overview").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isOk());
            mvc.perform(get("/api/v1/customers/4/overview").header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Cliente sem vínculo com a unidade responde 403 outra-concessionaria")
        void clienteForaDaCarteira() throws Exception {
            mvc.perform(get("/api/v1/customers/1/overview").header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.type").value(ERRORS + "outra-concessionaria"));
        }

        @Test
        @DisplayName("Cliente recém-cadastrado já entra na carteira de quem cadastrou (201 + Location)")
        void cadastroEntraNaCarteira() throws Exception {
            String location = mvc.perform(post("/api/v1/customers")
                            .header("Authorization", bearer(CONSULTOR_CAMPINAS))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(NOVO_CLIENTE))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", startsWith("http://localhost/api/v1/customers/")))
                    .andExpect(jsonPath("$.dadosPessoais.cpf").value("***.654.321-**"))
                    .andReturn().getResponse().getHeader("Location");

            mvc.perform(get(location.replace("http://localhost", "")).header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(status().isOk());
            mvc.perform(get(location.replace("http://localhost", "")).header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CPF já cadastrado responde 409 conflito")
        void cpfDuplicado() throws Exception {
            mvc.perform(post("/api/v1/customers")
                            .header("Authorization", bearer(CONSULTOR_MORUMBI))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(NOVO_CLIENTE.replace("98765432100", "11122233437")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value(ERRORS + "conflito"));
        }

        @Test
        @DisplayName("Cadastro com campo obrigatório ausente responde 422 listando o campo")
        void cadastroInvalido() throws Exception {
            mvc.perform(post("/api/v1/customers")
                            .header("Authorization", bearer(CONSULTOR_MORUMBI))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(NOVO_CLIENTE.replace("\"Walk-in Teste\"", "\"\"")))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.violacoes[0].campo").value("dadosPessoais.nome"));
        }
    }

    @Nested
    @DisplayName("Busca ?q=")
    class Busca {

        @Test
        @DisplayName("Por nome parcial, sem diferenciar maiúsculas")
        void porNome() throws Exception {
            mvc.perform(get("/api/v1/customers?q=carl").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(jsonPath("$.content[*].nome", containsInAnyOrder("Carlos Pereira")));
        }

        @Test
        @DisplayName("Por CPF com pontuação")
        void porCpf() throws Exception {
            mvc.perform(get("/api/v1/customers?q=111.222.334-37").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].documentoMascarado").value("***.222.334-**"));
        }

        @Test
        @DisplayName("A busca respeita a carteira: Campinas não encontra o Carlos")
        void buscaRespeitaCarteira() throws Exception {
            mvc.perform(get("/api/v1/customers?q=carlos").header("Authorization", bearer(CONSULTOR_CAMPINAS)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("Perfis")
    class Perfis {

        @Test
        @DisplayName("Consultor não inativa cliente (só GERENTE e ADMIN)")
        void consultorNaoInativa() throws Exception {
            mvc.perform(delete("/api/v1/customers/1").header("Authorization", bearer(CONSULTOR_MORUMBI)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.type").value(ERRORS + "perfil-sem-permissao"));
        }

        @Test
        @DisplayName("Gerente inativa cliente da carteira (204)")
        void gerenteInativa() throws Exception {
            mvc.perform(delete("/api/v1/customers/1").header("Authorization", bearer(GERENTE_MORUMBI)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Analista Ford não acessa clientes")
        void analistaSemAcesso() throws Exception {
            mvc.perform(get("/api/v1/customers").header("Authorization", bearer(ANALISTA)))
                    .andExpect(status().isForbidden());
        }
    }
}
