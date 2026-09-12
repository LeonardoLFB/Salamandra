-- ============================================================
-- SALAMANDRA - SCRIPT DE CRIAÇÃO DO BANCO DE DADOS
-- ============================================================
--
-- Sistema de gerenciamento da Salamandra Incensaria
--
-- Este arquivo contém a estrutura atual do banco utilizada
-- pela aplicação.
--
-- Banco de dados: PostgreSQL
-- ============================================================


-- ============================================================
-- TABELA: USUARIO
-- ============================================================

CREATE TABLE IF NOT EXISTS usuario (

    id_usuario SERIAL PRIMARY KEY,

    nome VARCHAR(120) NOT NULL,

    login VARCHAR(60) NOT NULL UNIQUE,

    senha VARCHAR(255) NOT NULL,

    email VARCHAR(120),

    tipo_acesso VARCHAR(30) NOT NULL
);


-- ============================================================
-- TABELA: CLIENTE
-- ============================================================

CREATE TABLE IF NOT EXISTS cliente (

    id_cliente SERIAL PRIMARY KEY,

    nome VARCHAR(120) NOT NULL,

    email VARCHAR(120),

    cpf VARCHAR(20) UNIQUE,

    rua VARCHAR(150),

    numero VARCHAR(20),

    bairro VARCHAR(100),

    cidade VARCHAR(100),

    estado VARCHAR(50),

    cep VARCHAR(20),

    data_cadastro TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- TABELA: FORNECEDOR
-- ============================================================

CREATE TABLE IF NOT EXISTS fornecedor (

    id_fornecedor SERIAL PRIMARY KEY,

    nome VARCHAR(120) NOT NULL,

    cnpj_cpf VARCHAR(30),

    contato VARCHAR(100),

    email VARCHAR(120),

    endereco VARCHAR(255),

    status VARCHAR(30)
        DEFAULT 'Ativo'
);


-- ============================================================
-- TABELA: PRODUTO
-- ============================================================

CREATE TABLE IF NOT EXISTS produto (

    id_produto SERIAL PRIMARY KEY,

    codigo INTEGER UNIQUE,

    nome VARCHAR(120) NOT NULL,

    lote VARCHAR(60),

    descricao TEXT,

    preco_custo NUMERIC(12,2)
        NOT NULL
        DEFAULT 0,

    preco_venda NUMERIC(12,2)
        NOT NULL
        DEFAULT 0,

    quantidade_estoque INTEGER
        NOT NULL
        DEFAULT 0,


    CONSTRAINT chk_produto_preco_custo
        CHECK (preco_custo >= 0),

    CONSTRAINT chk_produto_preco_venda
        CHECK (preco_venda >= 0),

    CONSTRAINT chk_produto_estoque
        CHECK (quantidade_estoque >= 0)
);


-- ============================================================
-- TABELA: VENDA
-- ============================================================

CREATE TABLE IF NOT EXISTS venda (

    id_venda SERIAL PRIMARY KEY,

    id_cliente INTEGER,

    data TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    valor_total NUMERIC(12,2)
        NOT NULL
        DEFAULT 0,

    status VARCHAR(30),

    observacao TEXT,

    id_usuario INTEGER,


    CONSTRAINT fk_venda_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
        ON DELETE SET NULL,

    CONSTRAINT fk_venda_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
);


-- ============================================================
-- TABELA: ITEM_VENDA
-- ============================================================

CREATE TABLE IF NOT EXISTS item_venda (

    id_item SERIAL PRIMARY KEY,

    id_venda INTEGER NOT NULL,

    id_produto INTEGER NOT NULL,

    quantidade INTEGER NOT NULL,

    preco_unitario NUMERIC(12,2) NOT NULL,

    subtotal NUMERIC(12,2) NOT NULL,


    CONSTRAINT fk_item_venda_venda
        FOREIGN KEY (id_venda)
        REFERENCES venda(id_venda)
        ON DELETE CASCADE,

    CONSTRAINT fk_item_venda_produto
        FOREIGN KEY (id_produto)
        REFERENCES produto(id_produto),


    CONSTRAINT chk_item_venda_quantidade
        CHECK (quantidade > 0),

    CONSTRAINT chk_item_venda_preco
        CHECK (preco_unitario >= 0),

    CONSTRAINT chk_item_venda_subtotal
        CHECK (subtotal >= 0)
);


-- ============================================================
-- TABELA: AUDITORIA
-- ============================================================

CREATE TABLE IF NOT EXISTS auditoria (

    id_auditoria SERIAL PRIMARY KEY,

    id_usuario INTEGER,

    acao VARCHAR(50) NOT NULL,

    descricao VARCHAR(255),

    data_hora TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
);


-- ============================================================
-- ÍNDICES - VENDA
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_venda_cliente
    ON venda(id_cliente);

CREATE INDEX IF NOT EXISTS idx_venda_usuario
    ON venda(id_usuario);

CREATE INDEX IF NOT EXISTS idx_venda_status
    ON venda(status);

CREATE INDEX IF NOT EXISTS idx_venda_data
    ON venda(data);


-- ============================================================
-- ÍNDICES - ITEM_VENDA
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_item_venda_venda
    ON item_venda(id_venda);

CREATE INDEX IF NOT EXISTS idx_item_venda_produto
    ON item_venda(id_produto);


-- ============================================================
-- ÍNDICES - AUDITORIA
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_auditoria_usuario
    ON auditoria(id_usuario);

CREATE INDEX IF NOT EXISTS idx_auditoria_data_hora
    ON auditoria(data_hora);


-- ============================================================
-- ÍNDICES - CLIENTE
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_cliente_nome
    ON cliente(nome);


-- ============================================================
-- ÍNDICES - PRODUTO
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_produto_nome
    ON produto(nome);


-- ============================================================
-- USUÁRIO ADMINISTRADOR PADRÃO
-- ============================================================

INSERT INTO usuario (
    nome,
    login,
    senha,
    email,
    tipo_acesso
)
SELECT
    'Administrador',
    'admin',
    'admin123',
    'admin@sistema.com',
    'Administrador'
WHERE NOT EXISTS (

    SELECT 1
    FROM usuario
    WHERE login = 'admin'
);


s