package br.com.fiap.vinsight_api.veiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DadosCadastroVeiculo(
        @NotBlank @Pattern(regexp = Vin.REGEX, message = Vin.MENSAGEM) String vin,
        @NotBlank @Size(min = 7, max = 7) String placa,
        @NotBlank @Size(max = 50) String modelo,
        @Size(max = 100) String versao,
        @NotNull Integer anoFabricacao,
        @NotNull Integer anoModelo,
        LocalDate dataCompra,
        @NotNull Long clienteId,
        Long concessionariaCompraId
) {
}
