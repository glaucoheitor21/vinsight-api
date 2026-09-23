-- US-33: cenario que demonstra a "carteira por relacionamento".
--
-- Na V900 todo cliente se relaciona com uma unidade so, entao qualquer regra de escopo daria o
-- mesmo resultado. Aqui o Carlos Pereira (cliente 1) ganha um vinculo com uma segunda unidade:
--
--   2022       comprou a Ranger na Ford Morumbi (1)        -> ja estava na V900
--   2024-2025  tres servicos na Ford Morumbi               -> ja estava na V900
--   2026-03    troca de oleo FORA da rede                  -> ja estava na V900
--   2026-08    revisao de 80.000 km na Ford CAMPINAS (2)   -> esta migration
--
-- Resultado esperado em GET /api/v1/customers/1/overview:
--   consultor de Morumbi  -> 200 (comprou la)
--   consultor de Campinas -> 200 (fez servico la)
--   consultor de P. Alegre-> 403 outra-concessionaria (nenhum vinculo)
INSERT INTO ordens_servico (veiculo_id, concessionaria_id, data_servico, tipo_servico, descricao,
                            valor, quilometragem, na_rede)
VALUES (1, 2, '2026-08-20', 'REVISAO_PROGRAMADA', 'Revisão de 80.000 km', 1250.00, 80000, TRUE);
