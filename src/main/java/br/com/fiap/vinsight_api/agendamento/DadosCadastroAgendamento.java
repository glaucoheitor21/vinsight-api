package br.com.fiap.vinsight_api.agendamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosCadastroAgendamento(
        @NotNull @Schema(example = "1", description = "id do veículo (campo id do passaporte em /vehicles/{vin})") Long veiculoId,
        @NotNull @Schema(example = "1", description = "Unidade do próprio usuário (usuario.concessionaria.id do login)") Long concessionariaId,
        @NotNull @Future @Schema(example = "2026-12-01T09:00:00") LocalDateTime dataHora,
        @NotNull @Schema(example = "REVISAO_PROGRAMADA") TipoServico tipoServico,
        @Size(max = 1000) @Schema(example = "Cliente pediu lavagem") String observacoes,
        @Schema(example = "850.00") BigDecimal valorEstimado
) {
}
