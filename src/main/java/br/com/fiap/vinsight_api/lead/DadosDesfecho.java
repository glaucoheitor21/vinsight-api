package br.com.fiap.vinsight_api.lead;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Corpo do PATCH /api/v1/leads/{id}, no formato do contrato:
 * { "desfecho": "AGENDADO", "observacao": "Cliente aceitou revisão", "proximoContato": "2026-10-02" }
 */
public record DadosDesfecho(
        @NotNull Desfecho desfecho,
        @Size(max = 500) String observacao,
        @FutureOrPresent LocalDate proximoContato
) {
}
