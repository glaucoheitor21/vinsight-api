package br.com.fiap.vinsight_api.lead;

import java.time.LocalDateTime;

public record DadosListagemLead(
        Long id,
        Long clienteId,
        String clienteNome,
        Long veiculoId,
        String veiculoPlaca,
        Double score,
        PrioridadeLead prioridade,
        StatusLead status,
        String motivo,
        LocalDateTime dataGeracao
) {
    public DadosListagemLead(Lead l) {
        this(l.getId(),
                l.getCliente().getId(),
                l.getCliente().getDadosPessoais().getNome(),
                l.getVeiculo().getId(),
                l.getVeiculo().getPlaca(),
                l.getScore(),
                l.getPrioridade(),
                l.getStatus(),
                l.getMotivo(),
                l.getDataGeracao());
    }
}
