package br.com.fiap.vinsight_api.concessionaria;

public record DadosListagemConcessionaria(
        Long id,
        String nomeFantasia,
        String cnpj,
        String cidade,
        String uf,
        boolean ativo
) {
    public DadosListagemConcessionaria(Concessionaria c) {
        this(c.getId(),
                c.getNomeFantasia(),
                c.getCnpj(),
                c.getEndereco().getCidade(),
                c.getEndereco().getUf(),
                c.isAtivo());
    }
}
