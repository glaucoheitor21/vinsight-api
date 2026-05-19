package br.com.fiap.vinsight_api.lead;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosCadastroLead(
        @NotNull Long clienteId,
        @NotNull Long veiculoId,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double score,
        @NotNull PrioridadeLead prioridade,
        @NotBlank @Size(max = 500) String motivo
) {
}
