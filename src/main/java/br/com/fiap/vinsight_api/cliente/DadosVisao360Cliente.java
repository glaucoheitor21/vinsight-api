package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.infra.security.MascaradorDados;
import br.com.fiap.vinsight_api.ordemservico.ResumoOrdens;
import br.com.fiap.vinsight_api.shared.FusoHorario;
import br.com.fiap.vinsight_api.veiculo.Veiculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/v1/customers/{id}/overview: tudo o que o consultor precisa antes de ligar, numa
 * chamada so, no formato exato do contrato.
 *
 * Os nomes "...Mascarado" sao do contrato e ficam fixos; para GERENTE/ADMIN o valor vem completo.
 */
public record DadosVisao360Cliente(
        Long id,
        String nome,
        String documentoMascarado,
        String telefoneMascarado,
        String emailMascarado,
        CanalContato canalPreferido,
        Consentimento consentimento,
        Integer ultimoNps,
        List<VeiculoResumo> veiculos,
        ResumoHistorico resumoHistorico
) {
    public record Consentimento(boolean ativo, List<CanalContato> canais, Instant atualizadoEm) {
    }

    public record VeiculoResumo(String vin, String modelo, Integer ano, String placa) {
        public VeiculoResumo(Veiculo v) {
            this(v.getVin(), v.getModelo(), v.getAnoModelo(), v.getPlaca());
        }
    }

    public record ResumoHistorico(long totalOrdens, BigDecimal ticketMedio, LocalDate ultimaVisita) {
        public ResumoHistorico(ResumoOrdens resumo) {
            this(resumo.total(),
                    resumo.valorMedio() == null ? null
                            : BigDecimal.valueOf(resumo.valorMedio()).setScale(2, RoundingMode.HALF_UP),
                    resumo.ultimaData());
        }
    }

    public DadosVisao360Cliente(Cliente c, List<Veiculo> veiculos, ResumoOrdens resumo,
                                MascaradorDados mascarador) {
        this(c.getId(),
                c.getDadosPessoais().getNome(),
                mascarador.cpf(c.getDadosPessoais().getCpf()),
                mascarador.telefone(c.getContato().getTelefone()),
                mascarador.email(c.getContato().getEmail()),
                c.getCanalPreferido(),
                new Consentimento(
                        c.isConsentimentoAtivo(),
                        c.getConsentimentoCanais(),
                        FusoHorario.emUtc(c.getConsentimentoAtualizadoEm())),
                c.getUltimoNps(),
                veiculos.stream().map(VeiculoResumo::new).toList(),
                new ResumoHistorico(resumo));
    }
}
