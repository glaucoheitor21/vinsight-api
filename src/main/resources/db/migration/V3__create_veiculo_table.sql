CREATE TABLE veiculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(17) NOT NULL UNIQUE,
    placa VARCHAR(7) NOT NULL UNIQUE,
    modelo VARCHAR(50) NOT NULL,
    versao VARCHAR(100),
    ano_fabricacao INT NOT NULL,
    ano_modelo INT NOT NULL,
    data_compra DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    cliente_id BIGINT NOT NULL,
    concessionaria_compra_id BIGINT,
    CONSTRAINT fk_veiculo_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_veiculo_concessionaria FOREIGN KEY (concessionaria_compra_id) REFERENCES concessionarias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_veiculo_vin ON veiculos(vin);
CREATE INDEX idx_veiculo_placa ON veiculos(placa);
CREATE INDEX idx_veiculo_cliente ON veiculos(cliente_id);
