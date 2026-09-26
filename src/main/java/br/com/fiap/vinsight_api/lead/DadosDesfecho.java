package br.com.fiap.vinsight_api.lead;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Corpo do PATCH /api/v1/leads/{id}, no formato do contrato:
 * { "desfecho": "AGENDADO", "observacao": "Cliente aceitou revisão", "proximoContato": "2026-10-02" }
 */
public record DadosDesfecho(
        @NotNull
        @Schema(example = "AGENDADO",
                description = "CONTATADO e SEM_SUCESSO mantêm o lead aberto; AGENDADO, RECUSADO e NUMERO_INVALIDO o encerram")
        Desfecho desfecho,

        @Size(max = 500)
        @Schema(example = "Cliente aceitou a revisão de 80.000 km")
        String observacao,

        @FutureOrPresent
        @Schema(example = "2026-12-01", description = "Quando voltar a ligar (usado com CONTATADO e SEM_SUCESSO)")
        LocalDate proximoContato
) {
}
