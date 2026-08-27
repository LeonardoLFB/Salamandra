-- ============================================================
-- Salamandra Incensaria - Script de criação do schema
-- PostgreSQL
--
-- Uso:
--   createdb -U postgres salamandra_incensaria
--   psql -U postgres -d salamandra_incensaria -f schema.sql
--
-- As colunas e nomes seguem exatamente os SQLs dos DAOs
-- (ClienteDAO, ProdutoDAO, UsuarioDAO, VendaDAO).
-- ============================================================

-- ------------------------------------------------------------
-- USUARIO
-- Usado por: UsuarioDAO (inserir, atualizar, deletar, getAll,
--            autenticar, criarUsuarioPadrao)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario   SERIAL       PRIMARY KEY,
    nome         VARCHAR(120) NOT NULL,
    login        VARCHAR(60)  NOT NULL UNIQUE,
    senha        VARCHAR(120) NOT NULL,
    email        VARCHAR(120),
    tipo_acesso  VARCHAR(40)  NOT NULL
);

-- ------------------------------------------------------------
-- CLIENTE
-- Usado por: ClienteDAO (inserir, atualizar, deletar, getAll,
--            buscarPorId)
-- data_cadastro tem DEFAULT porque o INSERT do DAO nao a informa,
-- mas o getAll/buscarPorId leem essa coluna.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente     SERIAL       PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    email          VARCHAR(120),
    cpf            VARCHAR(14),
    rua            VARCHAR(120),
    numero         VARCHAR(20),
    bairro         VARCHAR(80),
    cidade         VARCHAR(80),
    estado         VARCHAR(40),
    cep            VARCHAR(12),
    data_cadastro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- PRODUTO
-- Usado por: ProdutoDAO (inserir, deletar, getAll, toCSV) e
--            EstoqueController; VendaDAO da baixa em
--            quantidade_estoque ao registrar a venda.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    id_produto          SERIAL         PRIMARY KEY,
    codigo              INTEGER,
    nome                VARCHAR(120)   NOT NULL,
    lote                VARCHAR(60),
    descricao           TEXT,
    preco_custo         NUMERIC(12,2)  NOT NULL DEFAULT 0,
    preco_venda         NUMERIC(12,2)  NOT NULL DEFAULT 0,
    quantidade_estoque  INTEGER        NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- VENDA
-- Usado por: VendaDAO (inserir, atualizar, deletar, getAll,
--            buscarPorId, buscarPorCliente, buscarPorStatus)
-- id_cliente e FK para cliente; getAll faz LEFT JOIN, logo
-- ON DELETE SET NULL preserva o historico de vendas.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venda (
    id_venda     SERIAL         PRIMARY KEY,
    id_cliente   INTEGER,
    data         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total  NUMERIC(12,2)  NOT NULL DEFAULT 0,
    status       VARCHAR(40),
    observacao   TEXT,
    CONSTRAINT fk_venda_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- ITEM_VENDA
-- Usado por: VendaDAO (inserir em batch, buscarItensPorVenda)
-- ON DELETE CASCADE em id_venda e OBRIGATORIO: VendaDAO.deletar
-- apaga a venda contando com a remocao automatica dos itens.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_venda (
    id_item         SERIAL         PRIMARY KEY,
    id_venda        INTEGER        NOT NULL,
    id_produto      INTEGER        NOT NULL,
    quantidade      INTEGER        NOT NULL,
    preco_unitario  NUMERIC(12,2)  NOT NULL,
    subtotal        NUMERIC(12,2)  NOT NULL,
    CONSTRAINT fk_item_venda
        FOREIGN KEY (id_venda) REFERENCES venda (id_venda)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id_produto)
        ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- FORNECEDOR
-- Usado por: FornecedorDAO (inserir, atualizar, deletar, getAll,
--            buscarPorId). O model Fornecedor usa cnpjCpf -> cnpj_cpf.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fornecedor (
    id_fornecedor  SERIAL       PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    cnpj_cpf       VARCHAR(20),
    contato        VARCHAR(60),
    email          VARCHAR(120),
    endereco       VARCHAR(200),
    status         VARCHAR(20)  DEFAULT 'Ativo'
);

-- ------------------------------------------------------------
-- Indices auxiliares (consultas do VendaDAO)
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_venda_cliente     ON venda (id_cliente);
CREATE INDEX IF NOT EXISTS idx_venda_status      ON venda (status);
CREATE INDEX IF NOT EXISTS idx_item_venda_venda  ON item_venda (id_venda);

-- ------------------------------------------------------------
-- Usuario admin padrao
-- (o app tambem cria via UsuarioDAO.criarUsuarioPadrao;
--  aqui garantimos acesso mesmo em base recem-criada)
-- ------------------------------------------------------------
INSERT INTO usuario (nome, login, senha, email, tipo_acesso)
SELECT 'Administrador', 'admin', 'admin123', 'admin@sistema.com', 'Administrador'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE login = 'admin');
