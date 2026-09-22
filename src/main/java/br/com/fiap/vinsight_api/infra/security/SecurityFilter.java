package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.usuario.Usuario;
import br.com.fiap.vinsight_api.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Estrutura do SecurityFilter do professor, com duas protecoes a mais:
 *
 * 1. Token invalido/expirado NAO lanca excecao (no molde original isso virava 500).
 *    A requisicao segue sem autenticacao: rota protegida recebe 401 do
 *    AutenticacaoEntryPoint, rota publica funciona normalmente. Isso importa para o
 *    app mobile, que costuma mandar o access token vencido junto do /auth/refresh.
 * 2. Usuario apagado ou inativado depois de emitir o token nao autentica
 *    (no molde original, findByLogin nulo virava NullPointerException).
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    // Lido pelo AutenticacaoEntryPoint para explicar o motivo do 401
    public static final String ATRIBUTO_ERRO_TOKEN = "vinsight.erroToken";

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            try {
                Long usuarioId = tokenService.validarAccessToken(tokenJWT);
                repository.findById(usuarioId)
                        .filter(Usuario::isEnabled)
                        .ifPresent(usuario -> {
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    usuario.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            } catch (TokenInvalidoException ex) {
                request.setAttribute(ATRIBUTO_ERRO_TOKEN, ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }
        return null;
    }
}
