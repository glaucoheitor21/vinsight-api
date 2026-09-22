package br.com.fiap.vinsight_api.usuario;

import br.com.fiap.vinsight_api.concessionaria.Concessionaria;

/**
 * Bloco "usuario" da resposta de login/refresh, no formato do contrato.
 * concessionaria vem null para ANALISTA_FORD e ADMIN.
 */
public record DadosUsuarioLogado(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        DadosConcessionariaResumo concessionaria
) {
    public DadosUsuarioLogado(Usuario u) {
        this(u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil(),
                u.getConcessionaria() == null ? null : new DadosConcessionariaResumo(u.getConcessionaria()));
    }

    public record DadosConcessionariaResumo(Long id, String nome, String codigo) {
        public DadosConcessionariaResumo(Concessionaria c) {
            this(c.getId(), c.getNomeFantasia(), c.getCodigo());
        }
    }
}
