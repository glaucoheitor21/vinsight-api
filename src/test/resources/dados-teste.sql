-- Massa dos testes de integracao (US-52). Carregada antes de CADA teste, dentro da transacao do
-- teste, que e desfeita no fim: nenhum teste enxerga o que outro gravou.
--
-- O perfil "test" nao carrega o seed de demonstracao (V900), entao cada linha aqui existe para
-- provar um caso. Data de referencia dos testes: 22/09/2026 (RelogioFixoConfig).
--
-- Senhas (BCrypt, as mesmas do seed): consultor123 / gerente123 / analista123 / admin123

INSERT INTO concessionarias (id, nome_fantasia, codigo, razao_social, cnpj, logradouro, numero, complemento, bairro, cidade, uf, cep, email, telefone, opt_in_whats_app, ativo) VALUES
    (1, 'Ford Morumbi',  'SP-001', 'Morumbi Veiculos Ltda',  '11222333000181', 'Av. Giovanni Gronchi', '5900', NULL, 'Morumbi', 'Sao Paulo', 'SP', '05724003', 'morumbi@ford.com.br',  '1130115900', TRUE, TRUE),
    (2, 'Ford Campinas', 'SP-014', 'Campinas Veiculos Ltda', '22333444000172', 'Av. Norte Sul',        '1200', NULL, 'Cambui',  'Campinas',  'SP', '13025320', 'campinas@ford.com.br', '1932001200', TRUE, TRUE);

INSERT INTO usuarios (id, nome, email, senha, perfil, concessionaria_id, ativo, data_cadastro) VALUES
    (1, 'Ana Souza',     'consultor@ford.com.br',          '$2a$10$gM9.j8yj2Ku9L4rB4EDJ9eYEIIDo0AvPvky.9YwxTyP9lDHfN2C8a', 'CONSULTOR',     1,    TRUE,  '2026-01-15 09:00:00'),
    (2, 'Carla Nunes',   'consultor.campinas@ford.com.br', '$2a$10$gM9.j8yj2Ku9L4rB4EDJ9eYEIIDo0AvPvky.9YwxTyP9lDHfN2C8a', 'CONSULTOR',     2,    TRUE,  '2026-01-15 09:00:00'),
    (3, 'Eduardo Reis',  'gerente@ford.com.br',            '$2a$10$ktRfGhIFTnvbbzkGYCn/6eIoo9CoZKsuwO8B2cyQ/NDSugP/nO6kS', 'GERENTE',       1,    TRUE,  '2026-01-15 09:00:00'),
    (4, 'Gustavo Pinto', 'analista@ford.com.br',           '$2a$10$TVQxfAO9.N.UUpohYOvOlu.InbvAieI0MvqdN8Jgte6b6pEfDrbVO', 'ANALISTA_FORD', NULL, TRUE,  '2026-01-15 09:00:00'),
    (5, 'Helena Castro', 'admin@ford.com.br',              '$2a$10$DnAfMjLEifJRN3rZbEOGN.vYqRLSj4lUOMRqn3QEhU.KgcbzRSot2', 'ADMIN',         NULL, TRUE,  '2026-01-15 09:00:00'),
    -- Casos de borda da autenticacao e do escopo
    (6, 'Ivo Inativo',   'inativo@ford.com.br',            '$2a$10$gM9.j8yj2Ku9L4rB4EDJ9eYEIIDo0AvPvky.9YwxTyP9lDHfN2C8a', 'CONSULTOR',     1,    FALSE, '2026-01-15 09:00:00'),
    (7, 'Sergio Semloja','sem.unidade@ford.com.br',        '$2a$10$gM9.j8yj2Ku9L4rB4EDJ9eYEIIDo0AvPvky.9YwxTyP9lDHfN2C8a', 'CONSULTOR',     NULL, TRUE,  '2026-01-15 09:00:00');

-- 1 Carlos:  comprou em Morumbi                          -> carteira de Morumbi
-- 2 Mariana: comprou em Campinas                         -> carteira de Campinas
-- 3 Camila:  comprou em Morumbi, SEM consentimento LGPD  -> leads suprimidos
-- 4 Joao:    comprou em Morumbi, fez servico em Campinas -> carteira das DUAS unidades
INSERT INTO clientes (id, nome, cpf, data_nascimento, logradouro, numero, complemento, bairro, cidade, uf, cep, email, telefone, opt_in_whats_app, data_cadastro, ativo, consentimento_ativo, consentimento_canais, consentimento_atualizado_em, canal_preferido, ultimo_nps) VALUES
    (1, 'Carlos Pereira', '11122233437', '1972-06-12', 'Rua das Palmeiras', '103', NULL, 'Centro', 'Sao Paulo', 'SP', '05724007', 'carlos@email.com',  '11910004321', TRUE,  '2022-02-02 10:00:00', TRUE, TRUE,  'WHATSAPP,EMAIL', '2026-03-11 14:22:00', 'WHATSAPP', 9),
    (2, 'Mariana Rocha',  '22233344405', '1980-03-20', 'Rua Barao',         '45',  NULL, 'Cambui', 'Campinas',  'SP', '13025100', 'mariana@email.com', '19920005678', FALSE, '2023-03-03 10:00:00', TRUE, TRUE,  'EMAIL',          '2026-04-01 10:00:00', 'EMAIL',    8),
    (3, 'Camila Freitas', '33344455566', '1991-11-02', 'Rua Joao Cachoeira','300', NULL, 'Itaim',  'Sao Paulo', 'SP', '04535000', 'camila@email.com',  '11930007777', FALSE, '2024-04-04 10:00:00', TRUE, FALSE, NULL,             '2026-05-01 10:00:00', NULL,       NULL),
    (4, 'Joao Batista',   '44455566677', '1986-04-06', 'Rua das Flores',    '12',  NULL, 'Centro', 'Sao Paulo', 'SP', '05724021', 'joao@email.com',    '11940001111', TRUE,  '2024-05-05 10:00:00', TRUE, TRUE,  'TELEFONE',       '2026-02-01 10:00:00', 'TELEFONE', 6);

-- Veiculo 1 (Carlos): garantia vence em 15/11/2026 (54 dias -> PROXIMA_DO_FIM), revisao VENCIDA
--                     pela quilometragem (81.000 >= 80.000), telemetria com 2 codigos de falha
-- Veiculo 2 (Mariana): garantia ATIVA, revisao EM_DIA, sem telemetria
-- Veiculo 3 (Camila):  garantia ENCERRADA, revisao PROXIMA pela data (10/10/2026, 18 dias)
-- Veiculo 4 (Joao):    sem dados de garantia/revisao
INSERT INTO veiculos (id, vin, placa, modelo, versao, ano_fabricacao, ano_modelo, data_compra, status, cliente_id, concessionaria_compra_id, cor, quilometragem_estimada, garantia_data_limite, proxima_revisao_km, proxima_revisao_data, telemetria_recebida_em, telemetria_codigos_falha) VALUES
    (1, '9BF8313PF9WBWDX01', 'WCD1Z37', 'Ranger',         'Limited 3.0 V6', 2023, 2023, '2023-11-15', 'ATIVO', 1, 1, 'Preto',  81000, '2026-11-15', 80000, '2027-01-10', '2026-09-15 22:10:00', 'P0301,P0171'),
    (2, '9BFXR3MJLVF4SB8T9', 'ABC1D23', 'Maverick',       'Lariat',         2025, 2025, '2025-01-10', 'ATIVO', 2, 2, 'Branco', 30000, '2028-01-10', 60000, '2027-06-01', NULL,                  NULL),
    (3, '9BFC05GG9JY36J9B5', 'LFX5V55', 'Mustang Mach-E', 'GT',             2019, 2019, '2019-01-01', 'ATIVO', 3, 1, 'Azul',   50000, '2022-01-01', 60000, '2026-10-10', NULL,                  NULL),
    (4, '9BFJKD4NL04BHG2Z4', 'XBH4G28', 'Territory',      'Titanium',       2024, 2024, '2024-05-05', 'ATIVO', 4, 1, 'Cinza',  40000, NULL,         NULL,  NULL,         NULL,                  NULL);

-- Veiculo 1: 3 servicos na rede + 1 em oficina independente -> aderenciaRede 0,75
-- Veiculo 4: 1 servico em Campinas -> poe o Joao na carteira de Campinas
INSERT INTO ordens_servico (id, veiculo_id, concessionaria_id, data_servico, tipo_servico, descricao, valor, quilometragem, na_rede) VALUES
    (1, 1, 1,    '2025-03-10', 'REVISAO_PROGRAMADA', 'Revisao de 60.000 km', 900.00, 60000, TRUE),
    (2, 1, 1,    '2025-09-10', 'RECALL',             'Campanha de recall',   600.00, 68000, TRUE),
    (3, 1, 1,    '2026-01-10', 'GARANTIA',           'Atendimento em garantia', 500.00, 74000, TRUE),
    (4, 1, NULL, '2026-06-10', 'TROCA_OLEO',         'Troca de oleo',        400.00, 78000, FALSE),
    (5, 4, 2,    '2026-08-20', 'REVISAO_PROGRAMADA', 'Revisao de 40.000 km', 1000.00, 40000, TRUE);

INSERT INTO agendamentos (id, veiculo_id, concessionaria_id, data_hora, tipo_servico, status, observacoes, valor_estimado, data_criacao) VALUES
    (1, 1, 1, '2026-10-02 09:00:00', 'REVISAO_PROGRAMADA', 'AGENDADO', NULL, 800.00, '2026-09-10 11:00:00'),
    (2, 2, 2, '2026-10-03 10:00:00', 'REPARO',             'AGENDADO', NULL, 500.00, '2026-09-10 11:00:00'),
    (3, 4, 2, '2026-10-04 11:00:00', 'REVISAO_PROGRAMADA', 'AGENDADO', NULL, 900.00, '2026-09-10 11:00:00');

-- Fila de Morumbi: leads 1, 2, 5 e 6 (o 3 esta suprimido por LGPD). Abertos (OPEN): 1 e 2.
-- Fila de Campinas: lead 4.
INSERT INTO leads (id, cliente_id, veiculo_id, concessionaria_id, score, prioridade, status, motivo, data_geracao, data_conversao, motivo_contato, acao_recomendada, perfil_comportamental, ultimo_contato_em, suprimido_motivo, suprimido_em) VALUES
    (1, 1, 1, 1, 0.91, 'CRITICA', 'OPEN',      'Garantia encerra em 54 dias', '2026-09-15 08:00:00', NULL,                  'Garantia encerra em 54 dias', 'Oferecer extensao de garantia', 'ESQUECIDO', NULL,                  NULL,           NULL),
    (2, 4, 4, 1, 0.55, 'MEDIA',   'OPEN',      'Revisao de 50.000 km',        '2026-09-15 08:00:00', NULL,                  'Revisao de 50.000 km',        'Convidar para revisao',        'FIEL',      NULL,                  NULL,           NULL),
    (3, 3, 3, 1, 0.80, 'ALTA',    'OPEN',      'Revisao proxima',             '2026-09-15 08:00:00', NULL,                  'Revisao proxima',             'Ligar',                        'ABANDONO',  NULL,                  'LGPD_OPT_OUT', '2026-09-20 08:00:00'),
    (4, 2, 2, 2, 0.75, 'ALTA',    'OPEN',      'Revisao de 30.000 km',        '2026-09-15 08:00:00', NULL,                  'Revisao de 30.000 km',        'Ligar',                        'ECONOMICO', NULL,                  NULL,           NULL),
    (5, 1, 1, 1, 0.30, 'BAIXA',   'AGENDADO',  'Troca de oleo',               '2026-08-01 08:00:00', '2026-08-05 10:00:00', 'Troca de oleo',               'Ligar',                        'FIEL',      '2026-08-05 10:00:00', NULL,           NULL),
    (6, 4, 4, 1, 0.45, 'MEDIA',   'CONTATADO', 'Revisao atrasada',            '2026-09-10 08:00:00', NULL,                  'Revisao atrasada',            'Ligar de novo',                'ESQUECIDO', '2026-09-18 15:00:00', NULL,           NULL);
