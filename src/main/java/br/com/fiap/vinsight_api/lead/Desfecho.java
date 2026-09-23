package br.com.fiap.vinsight_api.lead;

/**
 * O que o consultor registra depois de um contato (PATCH /api/v1/leads/{id}).
 * Cada desfecho leva o lead ao status de mesmo nome. OPEN nao e desfecho: o app nao consegue
 * "reabrir" um lead (valor fora deste enum responde 422).
 */
public enum Desfecho {
    CONTATADO,
    AGENDADO,
    SEM_SUCESSO,
    RECUSADO,
    NUMERO_INVALIDO;

    public StatusLead status() {
        return StatusLead.valueOf(name());
    }
}
