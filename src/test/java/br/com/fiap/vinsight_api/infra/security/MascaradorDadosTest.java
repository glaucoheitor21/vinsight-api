package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.usuario.Perfil;
import br.com.fiap.vinsight_api.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Mascaramento por perfil (US-33, LGPD), testado com Mockito: o ContextoSeguranca e simulado,
 * sem subir o Spring nem autenticar ninguem.
 */
@ExtendWith(MockitoExtension.class)
class MascaradorDadosTest {

    @Mock
    private ContextoSeguranca contexto;

    @InjectMocks
    private MascaradorDados mascarador;

    private void logadoComo(Perfil perfil) {
        when(contexto.usuarioLogado()).thenReturn(new Usuario(1L, "Teste", "t@ford.com.br", "x", perfil, null, true, null));
    }

    @Test
    @DisplayName("Consultor recebe CPF, telefone e e-mail parcialmente mascarados, no formato do contrato")
    void consultorMascarado() {
        logadoComo(Perfil.CONSULTOR);

        assertThat(mascarador.cpf("12345678901")).isEqualTo("***.456.789-**");
        assertThat(mascarador.telefone("11910004321")).isEqualTo("(11) *****-4321");
        assertThat(mascarador.telefone("1130115900")).isEqualTo("(11) ****-5900");
        assertThat(mascarador.email("carlos.1@email.com")).isEqualTo("c****@email.com");
    }

    @Test
    @DisplayName("Gerente e admin recebem os dados completos")
    void gerenteEAdminCompletos() {
        for (Perfil perfil : new Perfil[]{Perfil.GERENTE, Perfil.ADMIN}) {
            logadoComo(perfil);
            assertThat(mascarador.cpf("12345678901")).isEqualTo("12345678901");
            assertThat(mascarador.telefone("11910004321")).isEqualTo("11910004321");
            assertThat(mascarador.email("carlos.1@email.com")).isEqualTo("carlos.1@email.com");
        }
    }

    @Test
    @DisplayName("Valores nulos ou fora do formato não quebram e não vazam o dado")
    void valoresForaDoPadrao() {
        logadoComo(Perfil.CONSULTOR);

        assertThat(mascarador.cpf(null)).isNull();
        assertThat(mascarador.cpf("123")).isEqualTo("***");
        assertThat(mascarador.telefone("123")).isEqualTo("****");
        assertThat(mascarador.email("sem-arroba")).isEqualTo("****");
    }
}
