package br.com.fiap.vinsight_api.shared;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DadosPessoais {

    @NotBlank
    @Size(max = 100)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}")
    private String cpf;

    @NotNull
    @Past
    private LocalDate dataNascimento;
}
