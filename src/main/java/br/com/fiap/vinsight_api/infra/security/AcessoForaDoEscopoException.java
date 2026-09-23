package br.com.fiap.vinsight_api.infra.security;

import org.springframework.security.access.AccessDeniedException;

/**
 * Perfil certo, unidade errada: o recurso pertence a outra concessionaria (US-30).
 * Separada do @PreAuthorize negado para o app receber um "type" proprio (outra-concessionaria).
 */
public class AcessoForaDoEscopoException extends AccessDeniedException {

    public AcessoForaDoEscopoException() {
        super("Recurso pertence a outra concessionária.");
    }
}
