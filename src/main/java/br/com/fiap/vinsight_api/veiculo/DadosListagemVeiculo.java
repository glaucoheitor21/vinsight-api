package br.com.fiap.vinsight_api.veiculo;

public record DadosListagemVeiculo(
        Long id,
        String vin,
        String placa,
        String modelo,
        String versao,
        Integer anoModelo,
        StatusVeiculo status,
        Long clienteId,
        String clienteNome
) {
    public DadosListagemVeiculo(Veiculo v) {
        this(v.getId(),
                v.getVin(),
                v.getPlaca(),
                v.getModelo(),
                v.getVersao(),
                v.getAnoModelo(),
                v.getStatus(),
                v.getCliente().getId(),
                v.getCliente().getDadosPessoais().getNome());
    }
}
