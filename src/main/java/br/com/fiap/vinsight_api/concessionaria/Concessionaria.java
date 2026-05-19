package br.com.fiap.vinsight_api.concessionaria;

import br.com.fiap.vinsight_api.shared.DadosContato;
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

@Entity
@Table(name = "concessionarias")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Concessionaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeFantasia;

    private String razaoSocial;

    private String cnpj;

    @Embedded
    private Endereco endereco;

    @Embedded
    private DadosContato contato;

    private boolean ativo;

    @PrePersist
    void preCadastro() {
        this.ativo = true;
    }

    public Concessionaria(DadosCadastroConcessionaria dados) {
        this.nomeFantasia = dados.nomeFantasia();
        this.razaoSocial = dados.razaoSocial();
        this.cnpj = dados.cnpj();
        this.endereco = dados.endereco();
        this.contato = dados.contato();
    }

    public void atualizar(DadosAtualizacaoConcessionaria dados) {
        if (dados.nomeFantasia() != null) this.nomeFantasia = dados.nomeFantasia();
        if (dados.razaoSocial() != null) this.razaoSocial = dados.razaoSocial();
        if (dados.endereco() != null) this.endereco = dados.endereco();
        if (dados.contato() != null) this.contato = dados.contato();
    }

    public void inativar() {
        this.ativo = false;
    }
}
