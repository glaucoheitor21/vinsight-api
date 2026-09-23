package br.com.fiap.vinsight_api.config;

import br.com.fiap.vinsight_api.shared.FusoHorario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * "Hoje" vem deste bean, e nao de LocalDate.now() direto: nos testes (US-52) basta trocar por um
 * Clock.fixed(...) para que garantia e revisao tenham status previsiveis.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(FusoHorario.BRASILIA);
    }
}
