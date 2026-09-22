-- US-28: campos do passaporte do veiculo (US-34).
-- garantia.status e proximaRevisao.situacao sao DERIVADOS em leitura a partir
-- destas datas, por isso nao ha coluna de status armazenada.
ALTER TABLE veiculos
    ADD COLUMN cor VARCHAR(30) NULL,
    ADD COLUMN quilometragem_estimada INT NULL,
    ADD COLUMN garantia_data_limite DATE NULL,
    ADD COLUMN proxima_revisao_km INT NULL,
    ADD COLUMN proxima_revisao_data DATE NULL,
    ADD COLUMN telemetria_recebida_em DATETIME(6) NULL,
    ADD COLUMN telemetria_codigos_falha VARCHAR(200) NULL;

CREATE INDEX idx_veiculo_garantia ON veiculos(garantia_data_limite);
