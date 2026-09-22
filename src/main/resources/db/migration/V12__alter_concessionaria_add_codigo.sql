-- US-28: o contrato devolve concessionaria { id, nome, codigo } na resposta de login.
-- A v1 nao tinha o codigo da unidade (ex.: SP-001).
ALTER TABLE concessionarias
    ADD COLUMN codigo VARCHAR(10) NULL;

ALTER TABLE concessionarias
    ADD CONSTRAINT uk_concessionaria_codigo UNIQUE (codigo);
