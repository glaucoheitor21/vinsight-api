package br.com.fiap.vinsight_api.agendamento;

import java.time.LocalDateTime;

public record DadosListagemAgendamento(
        Long id,
        LocalDateTime dataHora,
        TipoServico tipoServico,
        StatusAgendamento status,
        Long veiculoId,
        String veiculoPlaca,
        Long concessionariaId,
        String concessionariaNomeFantasia
) {
    public DadosListagemAgendamento(Agendamento a) {
        this(a.getId(),
                a.getDataHora(),
                a.getTipoServico(),
                a.getStatus(),
                a.getVeiculo().getId(),
                a.getVeiculo().getPlaca(),
                a.getConcessionaria().getId(),
                a.getConcessionaria().getNomeFantasia());
    }
}
