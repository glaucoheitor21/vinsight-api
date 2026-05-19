CREATE TABLE agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    concessionaria_id BIGINT NOT NULL,
    data_hora DATETIME(6) NOT NULL,
    tipo_servico VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    observacoes VARCHAR(1000),
    valor_estimado DECIMAL(10, 2),
    data_criacao DATETIME(6) NOT NULL,
    CONSTRAINT fk_agendamento_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    CONSTRAINT fk_agendamento_concessionaria FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_agendamento_veiculo ON agendamentos(veiculo_id);
CREATE INDEX idx_agendamento_concessionaria ON agendamentos(concessionaria_id);
CREATE INDEX idx_agendamento_data ON agendamentos(data_hora);
CREATE INDEX idx_agendamento_status ON agendamentos(status);
