-- US-35: Lead Engine.

-- 1. Supressao registrada: lead que nao pode ir para a fila, e por que.
--    Hoje o unico motivo e LGPD_OPT_OUT (cliente sem consentimento de contato).
ALTER TABLE leads
    ADD COLUMN suprimido_motivo VARCHAR(30) NULL,
    ADD COLUMN suprimido_em DATETIME(6) NULL;

-- 2. O vocabulario de status passou a ser o do contrato (OPEN, CONTATADO, AGENDADO, ...).
--    A conversao dos dados da massa de demonstracao esta em db/seed/V903.
ALTER TABLE leads
    ALTER COLUMN status SET DEFAULT 'OPEN';

-- 3. Historico de desfechos: cada contato registrado pelo consultor vira uma linha.
--    E a base que realimenta o modelo de churn (retreinamento), por isso nada e sobrescrito.
CREATE TABLE desfechos_lead (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    desfecho VARCHAR(20) NOT NULL,
    observacao VARCHAR(500),
    proximo_contato DATE,
    usuario_id BIGINT NOT NULL,
    registrado_em DATETIME(6) NOT NULL,
    CONSTRAINT fk_desfecho_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_desfecho_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_desfecho_lead ON desfechos_lead(lead_id);
CREATE INDEX idx_lead_suprimido ON leads(suprimido_motivo);
