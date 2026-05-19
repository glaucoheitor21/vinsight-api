package br.com.fiap.vinsight_api.agendamento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosCadastroAgendamento(
        @NotNull Long veiculoId,
        @NotNull Long concessionariaId,
        @NotNull @Future LocalDateTime dataHora,
        @NotNull TipoServico tipoServico,
        @Size(max = 1000) String observacoes,
        BigDecimal valorEstimado
) {
}
