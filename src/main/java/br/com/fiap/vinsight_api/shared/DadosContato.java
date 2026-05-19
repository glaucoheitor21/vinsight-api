package br.com.fiap.vinsight_api.shared;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DadosContato {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String telefone;

    private boolean optInWhatsApp;
}
