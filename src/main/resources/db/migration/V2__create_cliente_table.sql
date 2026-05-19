CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- DadosPessoais (embeddable)
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    -- Endereco (embeddable)
    logradouro VARCHAR(200) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(50),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    -- DadosContato (embeddable)
    email VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    opt_in_whats_app BOOLEAN NOT NULL DEFAULT FALSE,
    -- Campos próprios
    data_cadastro DATETIME(6) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_cliente_cpf ON clientes(cpf);
CREATE INDEX idx_cliente_ativo ON clientes(ativo);
