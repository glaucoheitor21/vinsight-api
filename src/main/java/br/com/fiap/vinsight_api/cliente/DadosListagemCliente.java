package br.com.fiap.vinsight_api.cliente;

public record DadosListagemCliente(
        Long id,
        String nome,
        String cpf,
        String email,
        String cidade,
        String uf
) {
    public DadosListagemCliente(Cliente c) {
        this(c.getId(),
                c.getDadosPessoais().getNome(),
                c.getDadosPessoais().getCpf(),
                c.getContato().getEmail(),
                c.getEndereco().getCidade(),
                c.getEndereco().getUf());
    }
}
