-- ============================================================
-- Salamandra Incensaria - Dados de exemplo (opcional)
--
-- Execute DEPOIS do schema.sql:
--   psql -U postgres -d salamandra_incensaria -f dados_exemplo.sql
--
-- O script e idempotente: cada bloco so insere se a tabela
-- estiver vazia, entao rodar duas vezes nao duplica registros.
-- ============================================================

SET client_encoding TO 'UTF8';

-- ------------------------------------------------------------
-- FORNECEDORES
-- ------------------------------------------------------------
INSERT INTO fornecedor (nome, cnpj_cpf, contato, email, endereco, status)
SELECT * FROM (VALUES
    ('Incensos do Oriente Ltda', '12.345.678/0001-90', '(11) 98765-4321', 'contato@oriente.com.br',    'Rua das Especiarias, 123 - São Paulo', 'Ativo'),
    ('Aromas Naturais ME',       '98.765.432/0001-10', '(21) 97654-3210', 'vendas@aromasnaturais.com', 'Av. Atlântica, 500 - Rio de Janeiro',  'Ativo'),
    ('Casa da Vela Artesanal',   '45.678.912/0001-33', '(31) 96543-2109', 'casa@veladeluz.com.br',     'Rua Minas, 78 - Belo Horizonte',       'Ativo'),
    ('Ervas & Resinas Import',   '32.165.498/0001-77', '(41) 95432-1098', 'import@ervaseresinas.com',  'Rua Curitiba, 900 - Curitiba',         'Inativo')
) AS v(nome, cnpj_cpf, contato, email, endereco, status)
WHERE NOT EXISTS (SELECT 1 FROM fornecedor);

-- ------------------------------------------------------------
-- CLIENTES  (CPFs ficticios, mas validos nos digitos verificadores,
--            para passar na validacao da tela de Clientes)
-- ------------------------------------------------------------
INSERT INTO cliente (nome, email, cpf, rua, numero, bairro, cidade, estado, cep)
SELECT * FROM (VALUES
    ('Ana Beatriz Souza', 'ana.souza@email.com',    '529.982.247-25', 'Rua das Flores', '120', 'Centro',     'São Paulo',      'SP', '01001-000'),
    ('Carlos Mendes',     'carlos.mendes@email.com','168.995.350-09', 'Av. Paulista',   '900', 'Bela Vista', 'São Paulo',      'SP', '01310-100'),
    ('Juliana Prado',     'ju.prado@email.com',     '390.533.447-05', 'Rua do Sol',     '45',  'Copacabana', 'Rio de Janeiro', 'RJ', '22010-000')
) AS v(nome, email, cpf, rua, numero, bairro, cidade, estado, cep)
WHERE NOT EXISTS (SELECT 1 FROM cliente);

-- ------------------------------------------------------------
-- PRODUTOS
-- Quantidades variadas de proposito, para exercitar os filtros
-- da tela de Estoque (Em estoque / Estoque baixo / Fora de estoque).
-- ------------------------------------------------------------
INSERT INTO produto (codigo, nome, lote, descricao, preco_custo, preco_venda, quantidade_estoque)
SELECT * FROM (VALUES
    (1001, 'Incenso Lavanda',         'L001', 'Caixa com 9 varetas aroma lavanda', 4.50,  12.90, 48),
    (1002, 'Incenso Sândalo',         'L002', 'Caixa com 9 varetas aroma sândalo', 5.20,  14.90, 30),
    (1003, 'Incenso Palo Santo',      'L003', 'Madeira natural importada',        18.00,  39.90,  8),
    (1004, 'Vela Aromática Baunilha', 'L004', 'Vela de soja 180g',                11.00,  29.90, 15),
    (1005, 'Suporte Cerâmica',        'L005', 'Suporte artesanal para incenso',    7.50,  19.90,  0),
    (1006, 'Defumador de Ervas',      'L006', 'Mix de ervas para defumação',       9.00,  24.90, 22)
) AS v(codigo, nome, lote, descricao, preco_custo, preco_venda, quantidade_estoque)
WHERE NOT EXISTS (SELECT 1 FROM produto);

-- ------------------------------------------------------------
-- Conferencia rapida
-- ------------------------------------------------------------
SELECT 'fornecedor' AS tabela, COUNT(*) AS registros FROM fornecedor
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL SELECT 'produto', COUNT(*) FROM produto
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario;
