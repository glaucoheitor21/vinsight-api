package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.infra.security.MascaradorDados;

/**
 * Item da busca GET /api/v1/customers?q=. Mascarado conforme o perfil, como a visao 360.
 */
public record DadosResumoCliente(
        Long id,
        String nome,
        String documentoMascarado,
        String telefoneMascarado,
        String cidade,
        String uf
) {
    public DadosResumoCliente(Cliente c, MascaradorDados mascarador) {
        this(c.getId(),
                c.getDadosPessoais().getNome(),
                mascarador.cpf(c.getDadosPessoais().getCpf()),
                mascarador.telefone(c.getContato().getTelefone()),
                c.getEndereco().getCidade(),
                c.getEndereco().getUf());
    }
}
