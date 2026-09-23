package br.com.fiap.vinsight_api.lead;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Lead gerado pelo modelo de churn (POST /api/v1/leads, so ADMIN: e a porta de entrada da
 * camada de inteligencia). "motivo" e o texto em linguagem de negocio que vira o motivoContato.
 */
public record DadosCadastroLead(
        @NotNull Long clienteId,
        @NotNull Long veiculoId,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double score,
        @NotNull PrioridadeLead prioridade,
        @NotBlank @Size(max = 500) String motivo,
        @Size(max = 300) String acaoRecomendada,
        PerfilComportamental perfilComportamental
) {
}
