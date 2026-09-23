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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

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

    // --- Passaporte do veiculo (V10). Status de garantia e situacao da revisao NAO sao colunas:
    // sao derivados em leitura pelos metodos abaixo, a partir destas datas.

    private String cor;

    private Integer quilometragemEstimada;

    private LocalDate garantiaDataLimite;

    private Integer proximaRevisaoKm;

    private LocalDate proximaRevisaoData;

    private LocalDateTime telemetriaRecebidaEm;

    // Codigos OBD-II separados por virgula (ex.: "P0301,P0171"); null = sem falhas
    private String telemetriaCodigosFalha;

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

    // ------------------------------------------------------------------ regras do passaporte (US-34)
    // Recebem "hoje" como parametro (em vez de LocalDate.now()) para serem testaveis com data fixa.

    /** Garantia "avisa" quando faltam este tanto de dias ou menos. */
    public static final int DIAS_AVISO_GARANTIA = 90;
    /** Revisao "proxima" quando falta este tanto de dias ou de km, o que vier primeiro. */
    public static final int DIAS_AVISO_REVISAO = 30;
    public static final int KM_AVISO_REVISAO = 1_000;

    public StatusGarantia statusGarantia(LocalDate hoje) {
        if (garantiaDataLimite == null) return null;
        if (hoje.isAfter(garantiaDataLimite)) return StatusGarantia.ENCERRADA;
        if (!garantiaDataLimite.isAfter(hoje.plusDays(DIAS_AVISO_GARANTIA))) return StatusGarantia.PROXIMA_DO_FIM;
        return StatusGarantia.ATIVA;
    }

    public Integer mesesRestantesGarantia(LocalDate hoje) {
        if (garantiaDataLimite == null) return null;
        return (int) Math.max(0, ChronoUnit.MONTHS.between(hoje, garantiaDataLimite));
    }

    /** VENCIDA se passou da data OU da quilometragem; PROXIMA se esta perto de uma das duas. */
    public SituacaoRevisao situacaoRevisao(LocalDate hoje) {
        boolean temData = proximaRevisaoData != null;
        boolean temKm = proximaRevisaoKm != null && quilometragemEstimada != null;
        if (!temData && !temKm) return null;

        if ((temData && hoje.isAfter(proximaRevisaoData))
                || (temKm && quilometragemEstimada >= proximaRevisaoKm)) {
            return SituacaoRevisao.VENCIDA;
        }
        if ((temData && !proximaRevisaoData.isAfter(hoje.plusDays(DIAS_AVISO_REVISAO)))
                || (temKm && proximaRevisaoKm - quilometragemEstimada <= KM_AVISO_REVISAO)) {
            return SituacaoRevisao.PROXIMA;
        }
        return SituacaoRevisao.EM_DIA;
    }

    public List<String> codigosFalhaTelemetria() {
        if (telemetriaCodigosFalha == null || telemetriaCodigosFalha.isBlank()) return List.of();
        return Arrays.stream(telemetriaCodigosFalha.split(",")).map(String::trim).toList();
    }

    public void inativar() {
        this.status = StatusVeiculo.INATIVO;
    }
}
