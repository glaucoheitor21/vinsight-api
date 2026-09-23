package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.usuario.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Trilha de auditoria de acessos negados (cenario BDD da US-30).
 *
 * Logger dedicado "AUDITORIA": filtre no log com
 *   Select-String AUDITORIA   (PowerShell)   ou   grep AUDITORIA   (bash)
 */
@Component
public class AuditoriaAcesso {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    public void registrarAcessoNegado(HttpServletRequest request, String motivo) {
        AUDITORIA.warn("ACESSO_NEGADO {} {} usuario[{}] motivo=\"{}\"",
                request.getMethod(), request.getRequestURI(), descreverUsuario(), motivo);
    }

    private String descreverUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            Long concessionariaId = usuario.getConcessionaria() == null ? null : usuario.getConcessionaria().getId();
            return "id=%d email=%s perfil=%s concessionariaId=%s".formatted(
                    usuario.getId(), usuario.getEmail(), usuario.getPerfil(), concessionariaId);
        }
        return "anonimo";
    }
}
