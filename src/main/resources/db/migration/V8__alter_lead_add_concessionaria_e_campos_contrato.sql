-- US-28: colunas exigidas pelo contrato v1 da fila de leads.
-- concessionaria_id e o que permite o escopo de dados por unidade (US-30).
-- Todas nullable: o codigo da v1 continua inserindo leads sem elas ate a US-35.
ALTER TABLE leads
    ADD COLUMN concessionaria_id BIGINT NULL,
    ADD COLUMN motivo_contato VARCHAR(500) NULL,
    ADD COLUMN acao_recomendada VARCHAR(300) NULL,
    ADD COLUMN perfil_comportamental VARCHAR(20) NULL,
    ADD COLUMN ultimo_contato_em DATETIME(6) NULL;

ALTER TABLE leads
    ADD CONSTRAINT fk_lead_concessionaria FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id);

CREATE INDEX idx_lead_concessionaria ON leads(concessionaria_id);
