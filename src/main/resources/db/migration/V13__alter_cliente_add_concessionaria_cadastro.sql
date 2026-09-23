-- US-33: unidade onde o cliente foi cadastrado.
-- E um dos quatro vinculos da "carteira por relacionamento" (escopo de dados da US-30):
-- um cliente recem-cadastrado, ainda sem veiculo nem servico, ja aparece para quem o cadastrou.
-- NULL para os clientes da massa de demonstracao (os outros vinculos os cobrem) e para
-- cadastros feitos por ADMIN.
ALTER TABLE clientes
    ADD COLUMN concessionaria_cadastro_id BIGINT NULL;

ALTER TABLE clientes
    ADD CONSTRAINT fk_cliente_concessionaria_cadastro
        FOREIGN KEY (concessionaria_cadastro_id) REFERENCES concessionarias(id);

CREATE INDEX idx_cliente_concessionaria_cadastro ON clientes(concessionaria_cadastro_id);
