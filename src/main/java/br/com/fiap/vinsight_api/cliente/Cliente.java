package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.DadosPessoais;
import br.com.fiap.vinsight_api.shared.Endereco;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
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
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private DadosPessoais dadosPessoais;

    @Embedded
    private Endereco endereco;

    @Embedded
    private DadosContato contato;

    private LocalDateTime dataCadastro;

    private boolean ativo;

    // --- Consentimento LGPD, canal e NPS (V9): alimentam a visao 360 (US-33)

    private boolean consentimentoAtivo;

    @Convert(converter = CanaisContatoConverter.class)
    private List<CanalContato> consentimentoCanais;

    private LocalDateTime consentimentoAtualizadoEm;

    @Enumerated(EnumType.STRING)
    private CanalContato canalPreferido;

    private Integer ultimoNps;

    // Unidade que cadastrou o cliente (V13): um dos vinculos da carteira por relacionamento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_cadastro_id")
    private Concessionaria concessionariaCadastro;

    @PrePersist
    void preCadastro() {
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }

    public Cliente(DadosCadastroCliente dados, Concessionaria concessionariaCadastro) {
        this.dadosPessoais = dados.dadosPessoais();
        this.endereco = dados.endereco();
        this.contato = dados.contato();
        this.concessionariaCadastro = concessionariaCadastro;
    }

    public void atualizar(DadosAtualizacaoCliente dados) {
        if (dados.dadosPessoais() != null) this.dadosPessoais = dados.dadosPessoais();
        if (dados.endereco() != null) this.endereco = dados.endereco();
        if (dados.contato() != null) this.contato = dados.contato();
    }

    public void inativar() {
        this.ativo = false;
    }
}
