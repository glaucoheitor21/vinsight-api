package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.DadosPessoais;
import br.com.fiap.vinsight_api.shared.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroCliente(
        @NotNull @Valid DadosPessoais dadosPessoais,
        @NotNull @Valid Endereco endereco,
        @NotNull @Valid DadosContato contato
) {
}
