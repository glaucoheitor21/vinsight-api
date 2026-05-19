package br.com.fiap.vinsight_api.agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosDetalheAgendamento(
        Long id,
        VeiculoResumo veiculo,
        ConcessionariaResumo concessionaria,
        LocalDateTime dataHora,
        TipoServico tipoServico,
        StatusAgendamento status,
        String observacoes,
        BigDecimal valorEstimado,
        LocalDateTime dataCriacao
) {
    public DadosDetalheAgendamento(Agendamento a) {
        this(a.getId(),
                new VeiculoResumo(a.getVeiculo().getId(),
                        a.getVeiculo().getVin(),
                        a.getVeiculo().getPlaca(),
                        a.getVeiculo().getModelo()),
                new ConcessionariaResumo(a.getConcessionaria().getId(),
                        a.getConcessionaria().getNomeFantasia()),
                a.getDataHora(),
                a.getTipoServico(),
                a.getStatus(),
                a.getObservacoes(),
                a.getValorEstimado(),
                a.getDataCriacao());
    }

    public record VeiculoResumo(Long id, String vin, String placa, String modelo) {
    }

    public record ConcessionariaResumo(Long id, String nomeFantasia) {
    }
}
