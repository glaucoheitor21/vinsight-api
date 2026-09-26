package br.com.fiap.vinsight_api.suporte;

import br.com.fiap.vinsight_api.infra.security.TokenService;
import br.com.fiap.vinsight_api.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base dos testes de integracao: sobe a aplicacao inteira contra o MySQL real (schema
 * vinsight_test, com as migrations de verdade) e faz requisicoes HTTP pelo MockMvc.
 *
 * - @Sql carrega dados-teste.sql antes de cada teste;
 * - @Transactional desfaz tudo no fim de cada teste (inclusive o que a API gravou), entao a
 *   ordem dos testes nao importa e o banco nao acumula lixo;
 * - bearer(email) gera um access token REAL pelo TokenService: a requisicao passa pelo mesmo
 *   SecurityFilter que o app usa, sem atalho de usuario simulado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql("/dados-teste.sql")
@Import(RelogioFixoConfig.class)
public abstract class TesteIntegracao {

    // Usuarios de dados-teste.sql
    protected static final String CONSULTOR_MORUMBI = "consultor@ford.com.br";
    protected static final String CONSULTOR_CAMPINAS = "consultor.campinas@ford.com.br";
    protected static final String GERENTE_MORUMBI = "gerente@ford.com.br";
    protected static final String ANALISTA = "analista@ford.com.br";
    protected static final String ADMIN = "admin@ford.com.br";
    protected static final String INATIVO = "inativo@ford.com.br";
    protected static final String SEM_UNIDADE = "sem.unidade@ford.com.br";

    protected static final String PROBLEM_JSON = "application/problem+json";
    protected static final String ERRORS = "https://vinsight.ford/errors/";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    /** Valor do header Authorization para o usuario de dados-teste.sql com este e-mail. */
    protected String bearer(String email) {
        return "Bearer " + tokenService.gerarAccessToken(usuarioRepository.findByEmail(email).orElseThrow());
    }
}
