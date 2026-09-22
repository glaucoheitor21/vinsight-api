package br.com.fiap.vinsight_api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Cadeia de seguranca stateless com JWT, no molde do projeto do professor.
 *
 * Publico: health check, login/refresh e Swagger. Todo o resto exige access token valido.
 * As regras por perfil ficam nos controllers com @PreAuthorize (US-30).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Autorizacao por perfil gerenciada nos controllers
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Autowired
    private AutenticacaoEntryPoint autenticacaoEntryPoint;

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
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers(
                                "/api-docs",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        // Encaminhamento interno do Spring para a pagina de erro
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(autenticacaoEntryPoint))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
