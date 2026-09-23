package br.com.fiap.vinsight_api.infra.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Um identificador por requisicao, para ligar a resposta de erro que o usuario viu a linha exata
 * do log do servidor.
 *
 * - Aceita o X-Correlation-Id enviado pelo cliente (se for seguro para log), senao gera um UUID
 * - Coloca no MDC: toda linha de log da requisicao sai com ele (logging.pattern.correlation)
 * - Devolve no header da resposta e no campo correlationId dos erros (GlobalExceptionHandler)
 *
 * Roda antes de tudo, inclusive da cadeia do Spring Security, para que 401/403 tambem o tenham.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String CHAVE_MDC = "correlationId";

    // Evita log injection: so letras, numeros e hifen, ate 64 caracteres
    private static final Pattern FORMATO_SEGURO = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String recebido = request.getHeader(HEADER);
        String correlationId = recebido != null && FORMATO_SEGURO.matcher(recebido).matches()
                ? recebido
                : UUID.randomUUID().toString();

        MDC.put(CHAVE_MDC, correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CHAVE_MDC);
        }
    }
}
