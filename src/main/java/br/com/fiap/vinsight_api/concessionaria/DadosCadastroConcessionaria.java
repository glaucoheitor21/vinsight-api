package br.com.fiap.vinsight_api.concessionaria;

import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.shared.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosCadastroConcessionaria(
        @NotBlank @Size(max = 100) String nomeFantasia,
        @NotBlank @Size(max = 150) String razaoSocial,
        @NotBlank @Pattern(regexp = "\\d{14}") String cnpj,
        @NotNull @Valid Endereco endereco,
        @NotNull @Valid DadosContato contato
) {
}
