package br.com.fiap.vinsight_api.infra.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Token JWT expirado, adulterado ou do tipo errado (refresh usado como access e vice-versa).
 * Por ser uma AuthenticationException, vira 401 tanto no filtro quanto no /auth/refresh.
 * "expirado" separa os dois "type" do contrato: token-expirado e token-invalido.
 */
public class TokenInvalidoException extends AuthenticationException {

    private final boolean expirado;

    public TokenInvalidoException(String mensagem, Throwable causa, boolean expirado) {
        super(mensagem, causa);
        this.expirado = expirado;
    }

    public boolean isExpirado() {
        return expirado;
    }
}
