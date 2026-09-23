package br.com.fiap.vinsight_api.infra.idempotencia;

import br.com.fiap.vinsight_api.infra.exception.CampoInvalidoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Header Idempotency-Key (contrato do PATCH /api/v1/leads/{id}).
 *
 * O app gera um UUID por acao do usuario e reenvia a MESMA chave se a rede cair. Na primeira
 * vez a acao roda e a resposta fica gravada; nas seguintes, a resposta gravada e devolvida sem
 * rodar a acao de novo. Assim um desfecho reenviado nao vira dois registros.
 *
 * Deve ser chamado dentro da transacao da acao: acao e registro sao gravados juntos, ou nenhum.
 */
@Component
public class ServicoIdempotencia {

    public record Resultado<T>(T corpo, boolean repetido) {
    }

    @Autowired
    private RequisicaoIdempotenteRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    /** Sem chave (null), apenas executa a acao, sem protecao contra reenvio. */
    public <T> Resultado<T> executar(String chave, String metodo, String recurso, Object corpoRequisicao,
                                     Class<T> tipoResposta, Supplier<T> acao) {
        if (chave == null || chave.isBlank()) {
            return new Resultado<>(acao.get(), false);
        }
        String hash = hash(corpoRequisicao);

        Optional<RequisicaoIdempotente> anterior = repository.findByChaveAndMetodoAndRecurso(chave, metodo, recurso);
        if (anterior.isPresent()) {
            if (!anterior.get().getHashPayload().equals(hash)) {
                throw new CampoInvalidoException("Idempotency-Key",
                        "chave já usada com outro corpo de requisição; gere uma chave nova para uma ação nova");
            }
            return new Resultado<>(objectMapper.readValue(anterior.get().getCorpoResposta(), tipoResposta), true);
        }

        T corpo = acao.get();
        repository.save(new RequisicaoIdempotente(null, chave, metodo, recurso, hash, 200,
                objectMapper.writeValueAsString(corpo), LocalDateTime.now(clock)));
        return new Resultado<>(corpo, false);
    }

    private String hash(Object corpoRequisicao) {
        try {
            byte[] json = objectMapper.writeValueAsString(corpoRequisicao).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
