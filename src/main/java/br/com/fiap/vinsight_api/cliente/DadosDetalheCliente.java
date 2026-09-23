package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.infra.security.MascaradorDados;
import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.DadosPessoais;
import br.com.fiap.vinsight_api.shared.Endereco;

import java.time.LocalDateTime;

/**
 * Cadastro completo do cliente (GET/POST/PUT). CPF, e-mail e telefone passam pelo
 * MascaradorDados: nunca devolva os embeddables da entidade direto, sempre copias.
 */
public record DadosDetalheCliente(
        Long id,
        DadosPessoais dadosPessoais,
        Endereco endereco,
        DadosContato contato,
        LocalDateTime dataCadastro,
        boolean ativo
) {
    public DadosDetalheCliente(Cliente c, MascaradorDados mascarador) {
        this(c.getId(),
                new DadosPessoais(
                        c.getDadosPessoais().getNome(),
                        mascarador.cpf(c.getDadosPessoais().getCpf()),
                        c.getDadosPessoais().getDataNascimento()),
                c.getEndereco(),
                new DadosContato(
                        mascarador.email(c.getContato().getEmail()),
                        mascarador.telefone(c.getContato().getTelefone()),
                        c.getContato().isOptInWhatsApp()),
                c.getDataCadastro(),
                c.isAtivo());
    }
}
