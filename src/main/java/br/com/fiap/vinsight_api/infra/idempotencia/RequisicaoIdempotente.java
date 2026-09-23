package br.com.fiap.vinsight_api.infra.idempotencia;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Resposta ja dada para uma Idempotency-Key (tabela da V11). */
@Entity
@Table(name = "requisicoes_idempotentes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequisicaoIdempotente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chave;

    private String metodo;

    private String recurso;

    // SHA-256 do corpo da requisicao: a mesma chave com outro corpo e erro do cliente
    private String hashPayload;

    private int statusResposta;

    private String corpoResposta;

    private LocalDateTime criadoEm;
}
