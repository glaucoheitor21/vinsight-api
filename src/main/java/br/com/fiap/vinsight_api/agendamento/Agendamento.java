package br.com.fiap.vinsight_api.agendamento;

import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_id", nullable = false)
    private Concessionaria concessionaria;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private TipoServico tipoServico;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    @Column(length = 1000)
    private String observacoes;

    private BigDecimal valorEstimado;

    private LocalDateTime dataCriacao;

    @PrePersist
    void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        if (this.status == null) this.status = StatusAgendamento.AGENDADO;
    }

    public Agendamento(DadosCadastroAgendamento dados, Veiculo veiculo, Concessionaria concessionaria) {
        this.veiculo = veiculo;
        this.concessionaria = concessionaria;
        this.dataHora = dados.dataHora();
        this.tipoServico = dados.tipoServico();
        this.observacoes = dados.observacoes();
        this.valorEstimado = dados.valorEstimado();
    }

    public void atualizarStatus(StatusAgendamento novoStatus) {
        this.status = novoStatus;
    }

    public void cancelar() {
        this.status = StatusAgendamento.CANCELADO;
    }
}
