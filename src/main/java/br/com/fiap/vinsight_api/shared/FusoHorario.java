package br.com.fiap.vinsight_api.shared;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Os DATETIME do banco estao no horario de Brasilia; o contrato expoe instantes em UTC
 * ("2026-09-14T22:10:00Z").
 */
public final class FusoHorario {

    public static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    private FusoHorario() {
    }

    public static Instant emUtc(LocalDateTime dataHoraDoBanco) {
        return dataHoraDoBanco == null ? null : dataHoraDoBanco.atZone(BRASILIA).toInstant();
    }
}
