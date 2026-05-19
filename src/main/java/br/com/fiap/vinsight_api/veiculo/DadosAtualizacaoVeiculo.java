package br.com.fiap.vinsight_api.veiculo;

import java.time.LocalDate;

public record DadosAtualizacaoVeiculo(
        String modelo,
        String versao,
        Integer anoModelo,
        LocalDate dataCompra,
        StatusVeiculo status,
        Long clienteId
) {
}
