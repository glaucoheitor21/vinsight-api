package br.com.fiap.vinsight_api.agendamento;

/**
 * Vocabulario do contrato, o mesmo da tabela ordens_servico (V7).
 * A v1 usava REVISAO; os agendamentos antigos sao convertidos pela V901 (massa de demonstracao).
 */
public enum TipoServico {
    REVISAO_PROGRAMADA,
    TROCA_OLEO,
    REPARO,
    GARANTIA,
    RECALL
}
