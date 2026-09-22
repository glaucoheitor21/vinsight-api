package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.usuario.DadosUsuarioLogado;

/**
 * Resposta de POST /api/v1/auth/login e /refresh, no formato exato do contrato.
 * expiresIn = validade do accessToken em segundos.
 */
public record DadosTokenJWT(
        String accessToken,
        String refreshToken,
        long expiresIn,
        DadosUsuarioLogado usuario
) {
}
