package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.usuario.Perfil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mascaramento de dados pessoais conforme o perfil (US-33, LGPD: minimizacao).
 *
 * Regra: CONSULTOR recebe CPF, telefone e e-mail parcialmente mascarados, como no exemplo do
 * contrato para a visao 360. GERENTE e ADMIN, que respondem pela unidade/sistema, recebem os
 * dados completos. Para mudar quem ve o que, altere so podeVerDadosCompletos().
 *
 * O app nunca desmascara nada: o que sai daqui e o que chega na tela.
 */
@Component
public class MascaradorDados {

    @Autowired
    private ContextoSeguranca contexto;

    public boolean podeVerDadosCompletos() {
        Perfil perfil = contexto.usuarioLogado().getPerfil();
        return perfil == Perfil.GERENTE || perfil == Perfil.ADMIN;
    }

    /** 12345678901 -> ***.456.789-** */
    public String cpf(String cpf) {
        if (cpf == null || podeVerDadosCompletos()) return cpf;
        if (cpf.length() != 11) return "***";
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }

    /** 11910004321 -> (11) *****-4321 */
    public String telefone(String telefone) {
        if (telefone == null || podeVerDadosCompletos()) return telefone;
        if (telefone.length() < 6) return "****";
        return "(" + telefone.substring(0, 2) + ") "
                + "*".repeat(telefone.length() - 6) + "-"
                + telefone.substring(telefone.length() - 4);
    }

    /** carlos.1@email.com -> c****@email.com */
    public String email(String email) {
        if (email == null || podeVerDadosCompletos()) return email;
        int arroba = email.indexOf('@');
        if (arroba < 1) return "****";
        return email.charAt(0) + "****" + email.substring(arroba);
    }
}
