package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.infra.security.MascaradorDados;
import br.com.fiap.vinsight_api.shared.FusoHorario;

import java.time.Instant;

/**
 * Item da fila de leads (GET /api/v1/leads), no formato exato do contrato.
 * faixaRisco e derivada do score; telefone mascarado conforme o perfil (MascaradorDados).
 */
public record DadosFilaLead(
        Long id,
        String vin,
        String placa,
        StatusLead status,
        Double score,
        FaixaRisco faixaRisco,
        String motivoContato,
        String acaoRecomendada,
        PerfilComportamental perfilComportamental,
        Instant geradoEm,
        ClienteResumo cliente,
        VeiculoResumo veiculo
) {
    public record ClienteResumo(Long id, String nome, String telefoneMascarado) {
    }

    public record VeiculoResumo(String modelo, String versao, Integer ano) {
    }

    public DadosFilaLead(Lead l, MascaradorDados mascarador) {
        this(l.getId(),
                l.getVeiculo().getVin(),
                l.getVeiculo().getPlaca(),
                l.getStatus(),
                l.getScore(),
                l.faixaRisco(),
                l.getMotivoContato(),
                l.getAcaoRecomendada(),
                l.getPerfilComportamental(),
                FusoHorario.emUtc(l.getDataGeracao()),
                new ClienteResumo(
                        l.getCliente().getId(),
                        l.getCliente().getDadosPessoais().getNome(),
                        mascarador.telefone(l.getCliente().getContato().getTelefone())),
                new VeiculoResumo(
                        l.getVeiculo().getModelo(),
                        l.getVeiculo().getVersao(),
                        l.getVeiculo().getAnoModelo()));
    }
}
