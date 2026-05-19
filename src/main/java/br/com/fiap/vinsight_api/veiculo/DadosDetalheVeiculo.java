package br.com.fiap.vinsight_api.veiculo;

import java.time.LocalDate;

public record DadosDetalheVeiculo(
        Long id,
        String vin,
        String placa,
        String modelo,
        String versao,
        Integer anoFabricacao,
        Integer anoModelo,
        LocalDate dataCompra,
        StatusVeiculo status,
        ClienteResumo cliente,
        ConcessionariaResumo concessionariaCompra
) {
    public DadosDetalheVeiculo(Veiculo v) {
        this(v.getId(),
                v.getVin(),
                v.getPlaca(),
                v.getModelo(),
                v.getVersao(),
                v.getAnoFabricacao(),
                v.getAnoModelo(),
                v.getDataCompra(),
                v.getStatus(),
                new ClienteResumo(v.getCliente().getId(),
                        v.getCliente().getDadosPessoais().getNome(),
                        v.getCliente().getDadosPessoais().getCpf()),
                v.getConcessionariaCompra() == null ? null :
                        new ConcessionariaResumo(v.getConcessionariaCompra().getId(),
                                v.getConcessionariaCompra().getNomeFantasia()));
    }

    public record ClienteResumo(Long id, String nome, String cpf) {
    }

    public record ConcessionariaResumo(Long id, String nomeFantasia) {
    }
}
