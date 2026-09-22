package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.infra.exception.GlobalExceptionHandler.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Resposta para rota protegida acessada sem token valido.
 *
 * Sem isto, o Spring Security responde 403 sem corpo (nao ha formLogin nem httpBasic
 * configurados), e o contrato pede 401. Usa o mesmo ErroResponse do GlobalExceptionHandler;
 * a US-31 troca os dois por application/problem+json.
 */
@Component
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object erroToken = request.getAttribute(SecurityFilter.ATRIBUTO_ERRO_TOKEN);
        String mensagem = erroToken != null
                ? erroToken.toString()
                : "Autenticação necessária. Envie o header Authorization: Bearer <accessToken>.";

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ErroResponse(LocalDateTime.now(), 401, "Unauthorized", mensagem, null));
    }
}
