-- US-28/US-29: usuarios da plataforma (consultor, gerente, analista Ford, admin).
-- concessionaria_id e NULL para ANALISTA_FORD e ADMIN, que nao pertencem a uma unidade.
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    concessionaria_id BIGINT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro DATETIME(6) NOT NULL,
    CONSTRAINT fk_usuario_concessionaria FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_usuario_concessionaria ON usuarios(concessionaria_id);
