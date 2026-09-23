package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.infra.security.MascaradorDados;
import br.com.fiap.vinsight_api.shared.FusoHorario;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/v1/leads/{id} e resposta do PATCH de desfecho ("o lead atualizado", no contrato).
 *
 * Mesmos campos do item da fila, mais o que so interessa na tela do lead: ultimo contato,
 * supressao (se houver) e o historico de desfechos, do mais recente para o mais antigo.
 *
 * Na v1 este DTO devolvia CPF e e-mail do cliente sem mascara; agora so o telefone, mascarado.
 */
public record DadosDetalheLead(
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
        DadosFilaLead.ClienteResumo cliente,
        DadosFilaLead.VeiculoResumo veiculo,
        Instant ultimoContatoEm,
        Supressao supressao,
        List<ItemDesfecho> desfechos
) {
    public record Supressao(MotivoSupressao motivo, Instant em) {
    }

    public record ItemDesfecho(Desfecho desfecho, String observacao, LocalDate proximoContato,
                               String registradoPor, Instant registradoEm) {
        public ItemDesfecho(DesfechoLead d) {
            this(d.getDesfecho(), d.getObservacao(), d.getProximoContato(),
                    d.getUsuario().getNome(), FusoHorario.emUtc(d.getRegistradoEm()));
        }
    }

    public DadosDetalheLead(Lead l, List<DesfechoLead> desfechos, MascaradorDados mascarador) {
        this(new DadosFilaLead(l, mascarador), l, desfechos);
    }

    private DadosDetalheLead(DadosFilaLead f, Lead l, List<DesfechoLead> desfechos) {
        this(f.id(), f.vin(), f.placa(), f.status(), f.score(), f.faixaRisco(), f.motivoContato(),
                f.acaoRecomendada(), f.perfilComportamental(), f.geradoEm(), f.cliente(), f.veiculo(),
                FusoHorario.emUtc(l.getUltimoContatoEm()),
                l.suprimido() ? new Supressao(l.getSuprimidoMotivo(), FusoHorario.emUtc(l.getSuprimidoEm())) : null,
                desfechos.stream().map(ItemDesfecho::new).toList());
    }
}
