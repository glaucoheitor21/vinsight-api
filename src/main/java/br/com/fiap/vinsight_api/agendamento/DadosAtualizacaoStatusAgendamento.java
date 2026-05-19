package br.com.fiap.vinsight_api.agendamento;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoStatusAgendamento(
        @NotNull StatusAgendamento status
) {
}
