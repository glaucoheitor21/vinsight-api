-- US-28: suporte ao header Idempotency-Key do PATCH /api/v1/leads/{id} (US-35).
-- E o que permite o app reenviar acoes pendentes apos reconexao sem duplicar registro.
CREATE TABLE requisicoes_idempotentes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(64) NOT NULL,
    metodo VARCHAR(10) NOT NULL,
    recurso VARCHAR(200) NOT NULL,
    hash_payload VARCHAR(64) NOT NULL,
    status_resposta INT NOT NULL,
    corpo_resposta TEXT,
    criado_em DATETIME(6) NOT NULL,
    CONSTRAINT uk_idempotencia UNIQUE (chave, metodo, recurso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_idempotencia_criado_em ON requisicoes_idempotentes(criado_em);
