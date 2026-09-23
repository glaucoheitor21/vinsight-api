package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.usuario.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Escopo de dados por concessionaria (US-30).
 *
 * O @PreAuthorize decide QUAL perfil acessa um endpoint. Esta classe decide QUAIS DADOS ele ve:
 * um CONSULTOR da unidade A tem o perfil certo para ler leads, mas nao os da unidade B.
 *
 * A concessionaria vem sempre do usuario autenticado (token), nunca de parametro da requisicao.
 */
@Component
public class ContextoSeguranca {

    public Usuario usuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        throw new AuthenticationCredentialsNotFoundException("Nenhum usuário autenticado.");
    }

    /**
     * Concessionaria que limita os dados do usuario logado.
     * null = rede inteira (ANALISTA_FORD e ADMIN).
     */
    public Long concessionariaEscopo() {
        Usuario usuario = usuarioLogado();
        return switch (usuario.getPerfil()) {
            case ANALISTA_FORD, ADMIN -> null;
            case CONSULTOR, GERENTE -> {
                // Consultor/gerente sem unidade e cadastro inconsistente: negar,
                // nunca cair no "null = ve tudo"
                if (usuario.getConcessionaria() == null) {
                    throw new AccessDeniedException("Usuário sem concessionária vinculada.");
                }
                yield usuario.getConcessionaria().getId();
            }
        };
    }

    /**
     * Para filtros de listagem que aceitam concessionariaId como parametro.
     * ANALISTA_FORD/ADMIN filtram livremente; CONSULTOR/GERENTE sempre recebem a propria
     * unidade, e pedir outra explicitamente e 403.
     */
    public Long restringirConcessionaria(Long concessionariaIdSolicitada) {
        Long escopo = concessionariaEscopo();
        if (escopo == null) {
            return concessionariaIdSolicitada;
        }
        if (concessionariaIdSolicitada != null) {
            verificarMesmaConcessionaria(escopo, concessionariaIdSolicitada);
        }
        return escopo;
    }

    /** Para leitura/alteracao de um recurso individual: 403 se for de outra unidade. */
    public void verificarAcesso(Concessionaria concessionariaDoRecurso) {
        verificarAcesso(concessionariaDoRecurso == null ? null : concessionariaDoRecurso.getId());
    }

    public void verificarAcesso(Long concessionariaIdDoRecurso) {
        Long escopo = concessionariaEscopo();
        if (escopo != null) {
            verificarMesmaConcessionaria(escopo, concessionariaIdDoRecurso);
        }
    }

    private void verificarMesmaConcessionaria(Long escopo, Long concessionariaId) {
        if (!escopo.equals(concessionariaId)) {
            throw new AcessoForaDoEscopoException();
        }
    }
}
