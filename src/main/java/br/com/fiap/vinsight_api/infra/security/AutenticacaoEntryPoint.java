package br.com.fiap.vinsight_api.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Resposta para rota protegida acessada sem token valido.
 *
 * Sem isto, o Spring Security responde 403 sem corpo (nao ha formLogin nem httpBasic
 * configurados), e o contrato pede 401.
 *
 * Nao monta o JSON aqui: repassa a excecao ao GlobalExceptionHandler, para que o 401 do filtro
 * saia no mesmo formato problem+json de todos os outros erros da API.
 */
@Component
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        // Token presente mas invalido/expirado: o SecurityFilter guardou o motivo real.
        // Sem token: o Spring manda InsufficientAuthenticationException
        Exception motivo = request.getAttribute(SecurityFilter.ATRIBUTO_ERRO_TOKEN) instanceof TokenInvalidoException ex
                ? ex
                : authException;
        resolver.resolveException(request, response, null, motivo);
    }
}
