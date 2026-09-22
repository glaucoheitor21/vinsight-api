-- US-28: consentimento LGPD, canal preferido e NPS, exigidos pela visao 360 (US-33).
-- consentimento_ativo alimenta a supressao LGPD_OPT_OUT da fila de leads (US-35).
ALTER TABLE clientes
    ADD COLUMN consentimento_ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN consentimento_canais VARCHAR(100) NULL,
    ADD COLUMN consentimento_atualizado_em DATETIME(6) NULL,
    ADD COLUMN canal_preferido VARCHAR(20) NULL,
    ADD COLUMN ultimo_nps INT NULL;

CREATE INDEX idx_cliente_consentimento ON clientes(consentimento_ativo);
