-- US-28: base do historico de servico. Pre-requisito de US-33 (resumoHistorico),
-- US-34 (historico e aderenciaRede) e US-36 (Service Share).
-- concessionaria_id e NULL quando o servico foi feito FORA da rede oficial:
-- e exatamente essa linha que derruba o VIN Share.
CREATE TABLE ordens_servico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    concessionaria_id BIGINT,
    data_servico DATE NOT NULL,
    tipo_servico VARCHAR(30) NOT NULL,
    descricao VARCHAR(300) NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    quilometragem INT,
    na_rede BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    CONSTRAINT fk_os_concessionaria FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_os_veiculo ON ordens_servico(veiculo_id);
CREATE INDEX idx_os_concessionaria ON ordens_servico(concessionaria_id);
CREATE INDEX idx_os_data ON ordens_servico(data_servico);
CREATE INDEX idx_os_na_rede ON ordens_servico(na_rede);
CREATE INDEX idx_os_tipo ON ordens_servico(tipo_servico);
