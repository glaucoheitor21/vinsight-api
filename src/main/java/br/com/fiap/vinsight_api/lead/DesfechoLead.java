package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.usuario.Usuario;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Um contato registrado pelo consultor (tabela da V14). Nunca e alterado nem apagado: o
 * historico completo de desfechos e o que realimenta o modelo de churn (retreinamento).
 */
@Entity
@Table(name = "desfechos_lead")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class DesfechoLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @Enumerated(EnumType.STRING)
    private Desfecho desfecho;

    private String observacao;

    private LocalDate proximoContato;

    // Quem registrou: trilha para auditoria e para o retreinamento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private LocalDateTime registradoEm;

    public DesfechoLead(Lead lead, DadosDesfecho dados, Usuario usuario, LocalDateTime agora) {
        this.lead = lead;
        this.desfecho = dados.desfecho();
        this.observacao = dados.observacao();
        this.proximoContato = dados.proximoContato();
        this.usuario = usuario;
        this.registradoEm = agora;
    }
}
