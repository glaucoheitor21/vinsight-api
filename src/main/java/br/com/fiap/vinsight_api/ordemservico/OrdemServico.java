package br.com.fiap.vinsight_api.ordemservico;

import br.com.fiap.vinsight_api.agendamento.TipoServico;
import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.veiculo.Veiculo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Historico de servico do veiculo (tabela da V7). Base do resumo historico da visao 360
 * (US-33), do passaporte do veiculo (US-34) e do Service Share (US-36).
 *
 * Somente leitura por enquanto: as ordens chegam pela massa de demonstracao, e nao ha endpoint
 * de cadastro no contrato.
 */
@Entity
@Table(name = "ordens_servico")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    // NULL = servico feito FORA da rede oficial: e o que derruba o VIN Share
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_id")
    private Concessionaria concessionaria;

    private LocalDate dataServico;

    @Enumerated(EnumType.STRING)
    private TipoServico tipoServico;

    private String descricao;

    private BigDecimal valor;

    private Integer quilometragem;

    private boolean naRede;
}
