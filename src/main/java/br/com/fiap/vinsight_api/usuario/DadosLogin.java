package br.com.fiap.vinsight_api.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Os exemplos sao um usuario real da massa de demonstracao: o "Try it out" do Swagger ja loga
public record DadosLogin(
        @NotBlank
        @Email
        @Schema(example = "consultor@ford.com.br")
        String email,

        @NotBlank
        @Schema(example = "consultor123")
        String senha
) {
}
