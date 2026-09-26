package br.com.fiap.vinsight_api.suporte;

import br.com.fiap.vinsight_api.shared.FusoHorario;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.LocalDate;

/**
 * "Hoje" congelado em 22/09/2026 para os testes: garantia, revisao e datas de desfecho ficam
 * previsiveis, qualquer que seja o dia em que a suite rodar. Substitui o Clock de ClockConfig.
 */
@TestConfiguration
public class RelogioFixoConfig {

    public static final LocalDate HOJE = LocalDate.of(2026, 9, 22);

    @Bean
    @Primary
    public Clock relogioFixo() {
        return Clock.fixed(HOJE.atTime(12, 0).atZone(FusoHorario.BRASILIA).toInstant(), FusoHorario.BRASILIA);
    }
}
