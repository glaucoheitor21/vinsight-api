package br.com.fiap.vinsight_api.veiculo;

import br.com.fiap.vinsight_api.agendamento.TipoServico;
import br.com.fiap.vinsight_api.ordemservico.OrdemServico;
import br.com.fiap.vinsight_api.shared.FusoHorario;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/v1/vehicles/{vin}: o "passaporte" do veiculo, no formato do contrato.
 *
 * Tudo o que e status (garantia, revisao) ou indicador (aderenciaRede) e calculado aqui, na
 * leitura, a partir das datas e do historico; nada disso e gravado no banco.
 *
 * "id" nao esta no contrato: e um campo a mais para o app poder criar agendamento
 * (POST /agendamentos pede veiculoId).
 */
public record DadosPassaporteVeiculo(
        Long id,
        String vin,
        String placa,
        String modelo,
        String versao,
        Integer ano,
        String cor,
        Integer quilometragemEstimada,
        Garantia garantia,
        ProximaRevisao proximaRevisaoPrevista,
        Double aderenciaRede,
        Telemetria ultimaTelemetria,
        List<ItemHistorico> historico
) {
    public record Garantia(StatusGarantia status, LocalDate dataLimite, Integer mesesRestantes) {
    }

    public record ProximaRevisao(Integer km, LocalDate dataEstimada, SituacaoRevisao situacao) {
    }

    public record Telemetria(Instant recebidaEm, List<String> codigosFalha) {
    }

    public record ItemHistorico(Long id, LocalDate data, TipoServico tipoServico, String descricao,
                                BigDecimal valor, String concessionaria, boolean naRede) {
        public ItemHistorico(OrdemServico o) {
            this(o.getId(), o.getDataServico(), o.getTipoServico(), o.getDescricao(), o.getValor(),
                    o.getConcessionaria() == null ? null : o.getConcessionaria().getNomeFantasia(),
                    o.isNaRede());
        }
    }

    /** @param historico ordens do veiculo, ja ordenadas da mais recente para a mais antiga */
    public DadosPassaporteVeiculo(Veiculo v, List<OrdemServico> historico, LocalDate hoje) {
        this(v.getId(),
                v.getVin(),
                v.getPlaca(),
                v.getModelo(),
                v.getVersao(),
                v.getAnoModelo(),
                v.getCor(),
                v.getQuilometragemEstimada(),
                v.getGarantiaDataLimite() == null ? null
                        : new Garantia(v.statusGarantia(hoje), v.getGarantiaDataLimite(), v.mesesRestantesGarantia(hoje)),
                v.getProximaRevisaoKm() == null && v.getProximaRevisaoData() == null ? null
                        : new ProximaRevisao(v.getProximaRevisaoKm(), v.getProximaRevisaoData(), v.situacaoRevisao(hoje)),
                aderenciaRede(historico),
                v.getTelemetriaRecebidaEm() == null ? null
                        : new Telemetria(FusoHorario.emUtc(v.getTelemetriaRecebidaEm()), v.codigosFalhaTelemetria()),
                historico.stream().map(ItemHistorico::new).toList());
    }

    /**
     * Fracao das ordens de servico feitas na rede Ford (0.0 a 1.0), insumo do Service Share.
     * Ex.: 4 servicos na rede e 1 em oficina independente -> 0.8. Sem historico -> null.
     */
    private static Double aderenciaRede(List<OrdemServico> historico) {
        if (historico.isEmpty()) return null;
        long naRede = historico.stream().filter(OrdemServico::isNaRede).count();
        return Math.round(100.0 * naRede / historico.size()) / 100.0;
    }
}
