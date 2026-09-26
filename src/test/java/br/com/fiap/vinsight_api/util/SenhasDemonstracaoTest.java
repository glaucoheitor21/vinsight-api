package br.com.fiap.vinsight_api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os hashes BCrypt gravados na massa de demonstracao (db/seed/V900 e src/test/resources/
 * dados-teste.sql) correspondem as senhas documentadas no README. Se um hash for trocado por
 * engano, o login da demonstracao quebra, e este teste avisa antes.
 *
 * Para gerar o hash de uma senha nova: new BCryptPasswordEncoder().encode("senha").
 */
class SenhasDemonstracaoTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "consultor123, $2a$10$gM9.j8yj2Ku9L4rB4EDJ9eYEIIDo0AvPvky.9YwxTyP9lDHfN2C8a",
            "gerente123,   $2a$10$ktRfGhIFTnvbbzkGYCn/6eIoo9CoZKsuwO8B2cyQ/NDSugP/nO6kS",
            "analista123,  $2a$10$TVQxfAO9.N.UUpohYOvOlu.InbvAieI0MvqdN8Jgte6b6pEfDrbVO",
            "admin123,     $2a$10$DnAfMjLEifJRN3rZbEOGN.vYqRLSj4lUOMRqn3QEhU.KgcbzRSot2"
    })
    @DisplayName("Hash da massa de demonstração confere com a senha documentada")
    void hashConfereComSenha(String senha, String hash) {
        assertThat(encoder.matches(senha, hash)).isTrue();
        assertThat(encoder.matches(senha + "x", hash)).isFalse();
    }
}
