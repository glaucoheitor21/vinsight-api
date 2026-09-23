-- US-35: ajusta a massa de demonstracao (V900) ao Lead Engine.
-- Fica em db/seed, depois da V900, para rodar depois dela em banco novo e em banco existente.

-- 1. Status da v1 -> vocabulario do contrato
UPDATE leads SET status = 'OPEN'      WHERE status = 'NOVO';
UPDATE leads SET status = 'CONTATADO' WHERE status = 'EM_CONTATO';
UPDATE leads SET status = 'AGENDADO'  WHERE status = 'CONVERTIDO';
UPDATE leads SET status = 'RECUSADO'  WHERE status = 'PERDIDO';

-- 2. Supressao LGPD dos leads ja existentes de clientes sem consentimento (5 leads na massa).
--    Leads novos sao suprimidos no proprio POST /api/v1/leads.
UPDATE leads l
    JOIN clientes c ON c.id = l.cliente_id
SET l.suprimido_motivo = 'LGPD_OPT_OUT',
    l.suprimido_em     = '2026-09-22 08:00:00'
WHERE c.consentimento_ativo = FALSE;
