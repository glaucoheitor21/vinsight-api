package br.com.fiap.vinsight_api.lead;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoStatusLead(
        @NotNull StatusLead status
) {
}
