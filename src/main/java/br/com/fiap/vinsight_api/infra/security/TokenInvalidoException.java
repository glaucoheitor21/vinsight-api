package br.com.fiap.vinsight_api.infra.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Token JWT expirado, adulterado ou do tipo errado (refresh usado como access e vice-versa).
 * Por ser uma AuthenticationException, vira 401 tanto no filtro quanto no /auth/refresh.
 */
public class TokenInvalidoException extends AuthenticationException {

    public TokenInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
