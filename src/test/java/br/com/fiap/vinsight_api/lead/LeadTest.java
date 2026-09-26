package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras do Lead Engine (US-35), sem Spring nem banco: faixa de risco e ciclo de vida do lead. */
class LeadTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 22, 10, 0);

    private static Lead lead(StatusLead status) {
        Lead lead = new Lead();
        ReflectionTestUtils.setField(lead, "status", status);
        ReflectionTestUtils.setField(lead, "score", 0.8);
        return lead;
    }

    @ParameterizedTest(name = "score {0} -> {1}")
    @CsvSource({
            "0.00, BAIXO", "0.39, BAIXO",
            "0.40, MEDIO", "0.69, MEDIO",
            "0.70, ALTO",  "1.00, ALTO"
    })
    @DisplayName("Faixa de risco derivada do score, nos limites do contrato")
    void faixaRisco(double score, FaixaRisco esperada) {
        assertThat(FaixaRisco.de(score)).isEqualTo(esperada);
    }

    @Test
    @DisplayName("AGENDADO encerra o lead e registra a conversão e o último contato")
    void agendadoEncerra() {
        Lead lead = lead(StatusLead.OPEN);

        lead.registrarDesfecho(Desfecho.AGENDADO, AGORA);

        assertThat(lead.getStatus()).isEqualTo(StatusLead.AGENDADO);
        assertThat(lead.getStatus().encerrado()).isTrue();
        assertThat(lead.getDataConversao()).isEqualTo(AGORA);
        assertThat(lead.getUltimoContatoEm()).isEqualTo(AGORA);
    }

    @ParameterizedTest
    @EnumSource(value = Desfecho.class, names = {"CONTATADO", "SEM_SUCESSO"})
    @DisplayName("CONTATADO e SEM_SUCESSO deixam o lead aberto, sem conversão")
    void desfechosQueMantemAberto(Desfecho desfecho) {
        Lead lead = lead(StatusLead.OPEN);

        lead.registrarDesfecho(desfecho, AGORA);

        assertThat(lead.getStatus().encerrado()).isFalse();
        assertThat(lead.getDataConversao()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = StatusLead.class, names = {"AGENDADO", "RECUSADO", "NUMERO_INVALIDO"})
    @DisplayName("Lead encerrado recusa qualquer novo desfecho")
    void encerradoRecusa(StatusLead encerrado) {
        Lead lead = lead(encerrado);

        assertThatThrownBy(() -> lead.registrarDesfecho(Desfecho.CONTATADO, AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("encerrado");
        assertThat(lead.getStatus()).isEqualTo(encerrado);
    }

    @Test
    @DisplayName("Lead suprimido por LGPD recusa desfecho, mesmo estando aberto")
    void suprimidoRecusa() {
        Lead lead = lead(StatusLead.OPEN);
        lead.suprimir(MotivoSupressao.LGPD_OPT_OUT, AGORA);

        assertThatThrownBy(() -> lead.registrarDesfecho(Desfecho.CONTATADO, AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("LGPD_OPT_OUT");
        assertThat(lead.getStatus()).isEqualTo(StatusLead.OPEN);
    }

    @Test
    @DisplayName("Cada desfecho leva ao status de mesmo nome")
    void desfechoViraStatus() {
        for (Desfecho d : Desfecho.values()) {
            assertThat(d.status().name()).isEqualTo(d.name());
        }
    }
}
