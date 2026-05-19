package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.DadosPessoais;
import br.com.fiap.vinsight_api.shared.Endereco;

import java.time.LocalDateTime;

public record DadosDetalheCliente(
        Long id,
        DadosPessoais dadosPessoais,
        Endereco endereco,
        DadosContato contato,
        LocalDateTime dataCadastro,
        boolean ativo
) {
    public DadosDetalheCliente(Cliente c) {
        this(c.getId(),
                c.getDadosPessoais(),
                c.getEndereco(),
                c.getContato(),
                c.getDataCadastro(),
                c.isAtivo());
    }
}
