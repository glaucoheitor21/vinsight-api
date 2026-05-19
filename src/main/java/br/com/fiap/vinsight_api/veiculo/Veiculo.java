package br.com.fiap.vinsight_api.veiculo;

import br.com.fiap.vinsight_api.cliente.Cliente;
import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
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

import java.time.LocalDate;

@Entity
@Table(name = "veiculos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 17)
    private String vin;

    @Column(unique = true, nullable = false, length = 7)
    private String placa;

    private String modelo;

    private String versao;

    private Integer anoFabricacao;

    private Integer anoModelo;

    private LocalDate dataCompra;

    @Enumerated(EnumType.STRING)
    private StatusVeiculo status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concessionaria_compra_id")
    private Concessionaria concessionariaCompra;

    @PrePersist
    void prePersist() {
        if (this.status == null) this.status = StatusVeiculo.ATIVO;
    }

    public Veiculo(DadosCadastroVeiculo dados, Cliente cliente, Concessionaria concessionaria) {
        this.vin = dados.vin();
        this.placa = dados.placa();
        this.modelo = dados.modelo();
        this.versao = dados.versao();
        this.anoFabricacao = dados.anoFabricacao();
        this.anoModelo = dados.anoModelo();
        this.dataCompra = dados.dataCompra();
        this.cliente = cliente;
        this.concessionariaCompra = concessionaria;
    }

    public void atualizar(DadosAtualizacaoVeiculo dados, Cliente novoCliente) {
        if (dados.modelo() != null) this.modelo = dados.modelo();
        if (dados.versao() != null) this.versao = dados.versao();
        if (dados.anoModelo() != null) this.anoModelo = dados.anoModelo();
        if (dados.dataCompra() != null) this.dataCompra = dados.dataCompra();
        if (dados.status() != null) this.status = dados.status();
        if (novoCliente != null) this.cliente = novoCliente;
    }

    public void inativar() {
        this.status = StatusVeiculo.INATIVO;
    }
}
