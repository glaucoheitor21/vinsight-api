package br.com.fiap.vinsight_api.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosRefresh(
        @NotBlank
        String refreshToken
) {
}
