package br.com.fiap.vinsight_api.usuario;

import br.com.fiap.vinsight_api.suporte.TesteIntegracao;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-29: login, refresh, emissao e validacao do JWT. */
class AuthControllerTest extends TesteIntegracao {

    @Value("${api.security.token.secret}")
    private String segredo;

    private MvcResult login(String email, String senha) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}""".formatted(email, senha)))
                .andReturn();
    }

    private String campo(MvcResult resposta, String nome) throws Exception {
        String json = resposta.getResponse().getContentAsString();
        return json.replaceAll("(?s).*\"" + nome + "\":\"([^\"]+)\".*", "$1");
    }

    @Test
    @DisplayName("Login válido devolve access e refresh token e o usuário no formato do contrato")
    void loginValido() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"consultor@ford.com.br","senha":"consultor123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.perfil").value("CONSULTOR"))
                .andExpect(jsonPath("$.usuario.concessionaria.codigo").value("SP-001"));
    }

    @Test
    @DisplayName("Access token carrega sub (id), email, perfil, concessionariaId, tipo e expira em 15 min")
    void claimsDoToken() throws Exception {
        DecodedJWT token = JWT.decode(campo(login(CONSULTOR_MORUMBI, "consultor123"), "accessToken"));

        assertThat(token.getHeaderClaim("typ").asString()).isEqualTo("JWT");
        assertThat(token.getSubject()).isEqualTo("1");
        assertThat(token.getClaim("email").asString()).isEqualTo(CONSULTOR_MORUMBI);
        assertThat(token.getClaim("perfil").asString()).isEqualTo("CONSULTOR");
        assertThat(token.getClaim("concessionariaId").asLong()).isEqualTo(1L);
        assertThat(token.getClaim("tipo").asString()).isEqualTo("ACCESS");
        assertThat(token.getExpiresAtAsInstant())
                .isBetween(Instant.now().plusSeconds(14 * 60), Instant.now().plusSeconds(15 * 60 + 5));
    }

    @Test
    @DisplayName("Analista e admin recebem concessionaria nula (visão da rede)")
    void usuarioDeRedeSemConcessionaria() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"analista@ford.com.br","senha":"analista123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.concessionaria").value(nullValue()));
    }

    @Test
    @DisplayName("Senha errada e e-mail inexistente dão a MESMA resposta 401 (não revela se o e-mail existe)")
    void credenciaisInvalidasNaoRevelamEmail() throws Exception {
        MvcResult senhaErrada = login(CONSULTOR_MORUMBI, "errada");
        MvcResult emailInexistente = login("ninguem@ford.com.br", "qualquer");

        for (MvcResult r : new MvcResult[]{senhaErrada, emailInexistente}) {
            assertThat(r.getResponse().getStatus()).isEqualTo(401);
            assertThat(r.getResponse().getContentType()).isEqualTo(PROBLEM_JSON);
            assertThat(r.getResponse().getContentAsString()).contains(ERRORS + "credenciais-invalidas");
        }
        assertThat(campo(senhaErrada, "detail")).isEqualTo(campo(emailInexistente, "detail"));
    }

    @Test
    @DisplayName("Usuário inativo não consegue logar")
    void usuarioInativo() throws Exception {
        assertThat(login(INATIVO, "consultor123").getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Login sem senha responde 422 apontando o campo")
    void loginSemSenha() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"consultor@ford.com.br"}"""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.type").value(ERRORS + "validacao"))
                .andExpect(jsonPath("$.violacoes[0].campo").value("senha"));
    }

    @Test
    @DisplayName("Refresh token válido gera um novo par de tokens")
    void refreshValido() throws Exception {
        String refresh = campo(login(CONSULTOR_MORUMBI, "consultor123"), "refreshToken");

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.usuario.email").value(CONSULTOR_MORUMBI));
    }

    @Test
    @DisplayName("Access token não vale como refresh token")
    void accessTokenNaoValeComoRefresh() throws Exception {
        String access = campo(login(CONSULTOR_MORUMBI, "consultor123"), "accessToken");

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + access + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(ERRORS + "token-invalido"));
    }

    @Test
    @DisplayName("Refresh token não vale como access token")
    void refreshTokenNaoValeComoAccess() throws Exception {
        String refresh = campo(login(CONSULTOR_MORUMBI, "consultor123"), "refreshToken");

        mvc.perform(get("/api/v1/leads").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(ERRORS + "token-invalido"));
    }

    @Test
    @DisplayName("Rota protegida sem token responde 401 nao-autenticado em problem+json")
    void semToken() throws Exception {
        mvc.perform(get("/api/v1/leads"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(ERRORS + "nao-autenticado"));
    }

    @Test
    @DisplayName("Token adulterado responde 401 token-invalido")
    void tokenAdulterado() throws Exception {
        mvc.perform(get("/api/v1/leads").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(ERRORS + "token-invalido"));
    }

    @Test
    @DisplayName("Token expirado responde 401 token-expirado")
    void tokenExpirado() throws Exception {
        String expirado = JWT.create()
                .withHeader(Map.of("typ", "JWT"))
                .withIssuer("vinsight-api")
                .withSubject("1")
                .withClaim("tipo", "ACCESS")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(segredo));

        mvc.perform(get("/api/v1/leads").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(ERRORS + "token-expirado"));
    }

    @Test
    @DisplayName("Endpoints públicos respondem sem token: health e Swagger")
    void endpointsPublicos() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api-docs")).andExpect(status().isOk());
    }
}
