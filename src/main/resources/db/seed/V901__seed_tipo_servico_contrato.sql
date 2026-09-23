-- US-31 (divergencia 5.2): TipoServico passou a usar o vocabulario do contrato.
-- A massa de demonstracao (V900) gravou agendamentos com o valor da v1, REVISAO.
--
-- Fica em db/seed, numerada depois da V900, de proposito: assim roda DEPOIS do seed tanto num
-- banco novo quanto num banco que ja tem a V900 aplicada. Nao edite a V900: mudar o checksum
-- obrigaria a apagar o banco nas duas maquinas.
UPDATE agendamentos
SET tipo_servico = 'REVISAO_PROGRAMADA'
WHERE tipo_servico = 'REVISAO';
