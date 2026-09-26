package br.com.fiap.vinsight_api.veiculo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Regras do passaporte (US-34), sem Spring nem banco: status derivados a partir de "hoje". */
class VeiculoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 22);

    private static Veiculo veiculo(LocalDate garantia, Integer kmAtual, Integer kmRevisao, LocalDate dataRevisao) {
        Veiculo v = new Veiculo();
        ReflectionTestUtils.setField(v, "garantiaDataLimite", garantia);
        ReflectionTestUtils.setField(v, "quilometragemEstimada", kmAtual);
        ReflectionTestUtils.setField(v, "proximaRevisaoKm", kmRevisao);
        ReflectionTestUtils.setField(v, "proximaRevisaoData", dataRevisao);
        return v;
    }

    @ParameterizedTest(name = "garantia até {0} -> {1}")
    @CsvSource({
            "2026-09-21, ENCERRADA",       // venceu ontem
            "2026-09-22, PROXIMA_DO_FIM",  // vence hoje
            "2026-12-21, PROXIMA_DO_FIM",  // exatamente 90 dias
            "2026-12-22, ATIVA",           // 91 dias
            "2028-01-01, ATIVA"
    })
    @DisplayName("Status da garantia com aviso a partir de 90 dias")
    void statusGarantia(LocalDate dataLimite, StatusGarantia esperado) {
        assertThat(veiculo(dataLimite, null, null, null).statusGarantia(HOJE)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Sem data-limite, a garantia não tem status nem meses restantes")
    void semGarantia() {
        Veiculo v = veiculo(null, null, null, null);
        assertThat(v.statusGarantia(HOJE)).isNull();
        assertThat(v.mesesRestantesGarantia(HOJE)).isNull();
    }

    @Test
    @DisplayName("Meses restantes contam meses completos e nunca ficam negativos")
    void mesesRestantes() {
        assertThat(veiculo(LocalDate.of(2026, 12, 22), null, null, null).mesesRestantesGarantia(HOJE)).isEqualTo(3);
        assertThat(veiculo(LocalDate.of(2020, 1, 1), null, null, null).mesesRestantesGarantia(HOJE)).isZero();
    }

    @ParameterizedTest(name = "km {0}/{1}, data {2} -> {3}")
    @CsvSource({
            "81000, 80000, 2027-06-01, VENCIDA",  // passou do km
            "50000, 80000, 2026-09-21, VENCIDA",  // passou da data
            "79000, 80000, 2027-06-01, PROXIMA",  // faltam exatamente 1.000 km
            "50000, 80000, 2026-10-22, PROXIMA",  // faltam exatamente 30 dias
            "50000, 80000, 2026-10-23, EM_DIA",   // 31 dias e 30.000 km
    })
    @DisplayName("Situação da revisão: vale o que chegar primeiro, data ou quilometragem")
    void situacaoRevisao(int kmAtual, int kmRevisao, LocalDate dataRevisao, SituacaoRevisao esperada) {
        assertThat(veiculo(null, kmAtual, kmRevisao, dataRevisao).situacaoRevisao(HOJE)).isEqualTo(esperada);
    }

    @Test
    @DisplayName("Revisão só por data, sem quilometragem conhecida")
    void revisaoSoPorData() {
        assertThat(veiculo(null, null, null, LocalDate.of(2027, 1, 1)).situacaoRevisao(HOJE))
                .isEqualTo(SituacaoRevisao.EM_DIA);
    }

    @Test
    @DisplayName("Sem previsão de revisão, não há situação")
    void semPrevisao() {
        assertThat(veiculo(null, 50000, null, null).situacaoRevisao(HOJE)).isNull();
    }

    @Test
    @DisplayName("Códigos de falha da telemetria viram lista; sem códigos, lista vazia")
    void codigosDeFalha() {
        Veiculo v = new Veiculo();
        assertThat(v.codigosFalhaTelemetria()).isEmpty();
        ReflectionTestUtils.setField(v, "telemetriaCodigosFalha", "P0301, P0171");
        assertThat(v.codigosFalhaTelemetria()).containsExactly("P0301", "P0171");
    }
}
