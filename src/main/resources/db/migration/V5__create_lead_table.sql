CREATE TABLE leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOVO',
    motivo VARCHAR(500) NOT NULL,
    data_geracao DATETIME(6) NOT NULL,
    data_conversao DATETIME(6),
    CONSTRAINT fk_lead_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_lead_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_lead_cliente ON leads(cliente_id);
CREATE INDEX idx_lead_veiculo ON leads(veiculo_id);
CREATE INDEX idx_lead_prioridade ON leads(prioridade);
CREATE INDEX idx_lead_status ON leads(status);
CREATE INDEX idx_lead_score ON leads(score);
