package br.com.fiap.vinsight_api.concessionaria;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.Endereco;

public record DadosDetalheConcessionaria(
        Long id,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        Endereco endereco,
        DadosContato contato,
        boolean ativo
) {
    public DadosDetalheConcessionaria(Concessionaria c) {
        this(c.getId(),
                c.getNomeFantasia(),
                c.getRazaoSocial(),
                c.getCnpj(),
                c.getEndereco(),
                c.getContato(),
                c.isAtivo());
    }
}
