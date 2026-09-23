package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.cliente.Cliente;
import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import br.com.fiap.vinsight_api.veiculo.Veiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    // Unidade dona do lead: base do escopo de dados da US-30 (coluna criada na V8)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_id")
    private Concessionaria concessionaria;

    // Probabilidade de evasao (0.0 a 1.0) produzida pelo modelo de churn
    private Double score;

    // Classificacao da v1, ainda exigida pela coluna (NOT NULL). O contrato usa faixaRisco().
    @Enumerated(EnumType.STRING)
    private PrioridadeLead prioridade;

    @Enumerated(EnumType.STRING)
    private StatusLead status;

    // Texto livre da v1 (coluna NOT NULL); o contrato expoe motivoContato
    @Column(length = 500)
    private String motivo;

    // --- Campos do contrato (V8)

    @Column(length = 500)
    private String motivoContato;

    private String acaoRecomendada;

    @Enumerated(EnumType.STRING)
    private PerfilComportamental perfilComportamental;

    private LocalDateTime ultimoContatoEm;

    // --- Supressao registrada (V14): lead fora da fila, e por que

    @Enumerated(EnumType.STRING)
    private MotivoSupressao suprimidoMotivo;

    private LocalDateTime suprimidoEm;

    private LocalDateTime dataGeracao;

    // Quando o lead virou agendamento (desfecho AGENDADO)
    private LocalDateTime dataConversao;

    @PrePersist
    void prePersist() {
        if (this.dataGeracao == null) this.dataGeracao = LocalDateTime.now();
        if (this.status == null) this.status = StatusLead.OPEN;
    }

    public Lead(DadosCadastroLead dados, Cliente cliente, Veiculo veiculo) {
        this.cliente = cliente;
        this.veiculo = veiculo;
        // O lead pertence a unidade que vendeu o veiculo (mesma regra da massa de demonstracao)
        this.concessionaria = veiculo.getConcessionariaCompra();
        this.score = dados.score();
        this.prioridade = dados.prioridade();
        this.motivo = dados.motivo();
        this.motivoContato = dados.motivo();
        this.acaoRecomendada = dados.acaoRecomendada();
        this.perfilComportamental = dados.perfilComportamental();
    }

    public FaixaRisco faixaRisco() {
        return FaixaRisco.de(score);
    }

    public boolean suprimido() {
        return suprimidoMotivo != null;
    }

    public void suprimir(MotivoSupressao motivo, LocalDateTime agora) {
        this.suprimidoMotivo = motivo;
        this.suprimidoEm = agora;
    }

    /**
     * Aplica o desfecho de um contato. Lead encerrado ou suprimido nao aceita desfecho (409):
     * o encerrado ja foi resolvido, e o suprimido nao pode ser contatado (LGPD).
     */
    public void registrarDesfecho(Desfecho desfecho, LocalDateTime agora) {
        if (suprimido()) {
            throw new RegraNegocioException(
                    "Lead suprimido (" + suprimidoMotivo + "): o cliente não pode ser contatado.");
        }
        if (status.encerrado()) {
            throw new RegraNegocioException(
                    "Lead já encerrado com desfecho " + status + "; não aceita novo desfecho.");
        }
        this.status = desfecho.status();
        this.ultimoContatoEm = agora;
        if (desfecho == Desfecho.AGENDADO) {
            this.dataConversao = agora;
        }
    }
}
