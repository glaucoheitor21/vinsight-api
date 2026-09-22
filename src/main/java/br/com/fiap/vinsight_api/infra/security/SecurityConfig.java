package br.com.fiap.vinsight_api.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * US-28 — esqueleto da cadeia de seguranca.
 *
 * ATENCAO: nesta etapa a cadeia esta com anyRequest().permitAll() de proposito.
 * O starter de security ja esta no classpath (para o PasswordEncoder e para a
 * US-29), mas ainda nao existe endpoint de login nem filtro de JWT — se a cadeia
 * exigisse autenticacao agora, a API inteira ficaria inacessivel.
 *
 * A US-29 substitui o permitAll pelas regras reais e registra o SecurityFilter.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration configuration = new CorsConfiguration();
                            // Origens do app Expo e do dashboard web em desenvolvimento
                            configuration.setAllowedOriginPatterns(Arrays.asList(
                                    "http://localhost:*",
                                    "http://127.0.0.1:*",
                                    "exp://*"
                            ));
                            configuration.setAllowedMethods(List.of(
                                    "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
                            ));
                            configuration.setAllowedHeaders(List.of(
                                    "Authorization", "Content-Type", "Accept", "Origin", "Idempotency-Key"
                            ));
                            configuration.setAllowCredentials(true);
                            return configuration;
                        }))
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // TODO US-29: trocar pelas regras reais
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
