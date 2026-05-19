package br.com.fiap.vinsight_api.concessionaria;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.Endereco;
import jakarta.validation.Valid;

public record DadosAtualizacaoConcessionaria(
        String nomeFantasia,
        String razaoSocial,
        @Valid Endereco endereco,
        @Valid DadosContato contato
) {
}
