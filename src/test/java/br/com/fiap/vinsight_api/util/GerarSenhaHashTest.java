package br.com.fiap.vinsight_api.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitario (nao e um teste de regra de negocio): imprime os hashes BCrypt
 * usados na massa de demonstracao em db/seed.
 *
 * Rode com: ./mvnw test -Dtest=GerarSenhaHashTest
 */
class GerarSenhaHashTest {

    @Test
    void gerarHashes() {
        var encoder = new BCryptPasswordEncoder();
        for (String senha : new String[]{"consultor123", "gerente123", "analista123", "admin123"}) {
            System.out.println("HASH|" + senha + "|" + encoder.encode(senha));
        }
    }
}
