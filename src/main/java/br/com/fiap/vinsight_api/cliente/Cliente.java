package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.DadosPessoais;
import br.com.fiap.vinsight_api.shared.Endereco;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @PrePersist
    void preCadastro() {
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
    }

    public Cliente(DadosCadastroCliente dados) {
        this.dadosPessoais = dados.dadosPessoais();
        this.endereco = dados.endereco();
        this.contato = dados.contato();
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
