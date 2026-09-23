package br.com.fiap.vinsight_api.lead;

import java.util.EnumSet;
import java.util.Set;

/**
 * Vocabulario do contrato. OPEN = na fila, nunca trabalhado; os demais sao desfechos.
 *
 * Abertos (aceitam novo desfecho): OPEN, CONTATADO, SEM_SUCESSO.
 * Encerrados: AGENDADO, RECUSADO, NUMERO_INVALIDO.
 * A v1 usava NOVO/EM_CONTATO/CONVERTIDO/PERDIDO; a conversao dos dados esta em db/seed/V903.
 */
public enum StatusLead {
    OPEN,
    CONTATADO,
    AGENDADO,
    SEM_SUCESSO,
    RECUSADO,
    NUMERO_INVALIDO;

    private static final Set<StatusLead> ENCERRADOS = EnumSet.of(AGENDADO, RECUSADO, NUMERO_INVALIDO);

    public boolean encerrado() {
        return ENCERRADOS.contains(this);
    }
}
