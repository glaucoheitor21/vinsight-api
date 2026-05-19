package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.DadosPessoais;
import br.com.fiap.vinsight_api.shared.Endereco;
import jakarta.validation.Valid;

public record DadosAtualizacaoCliente(
        @Valid DadosPessoais dadosPessoais,
        @Valid Endereco endereco,
        @Valid DadosContato contato
) {
}
