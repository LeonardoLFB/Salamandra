-- ============================================================
-- Salamandra Incensaria - Vendas de exemplo (opcional)
--
-- Execute DEPOIS de schema.sql e dados_exemplo.sql:
--   psql -U postgres -d salamandra_incensaria -f dados_vendas.sql
--
-- Cria 6 vendas com seus itens, cobrindo os tres status que a
-- tela de Vendas filtra (Pendente / Concluida / Cancelada) e as
-- formas de pagamento aceitas pelo sistema.
--
-- Os IDs de cliente e produto sao buscados pelo CPF e pelo codigo
-- (e nao fixados na mao), entao o script funciona em qualquer
-- banco onde dados_exemplo.sql tenha sido aplicado.
--
-- O bloco inteiro so roda se a tabela venda estiver vazia, logo
-- executar duas vezes nao duplica nada.
-- ============================================================

SET client_encoding TO 'UTF8';

DO $$
DECLARE
    c_ana    INT; c_carlos INT; c_ju INT;
    p1001 INT; p1002 INT; p1003 INT; p1004 INT; p1006 INT;
    v_id  INT;
BEGIN
    IF EXISTS (SELECT 1 FROM venda) THEN
        RAISE NOTICE 'Ja existem vendas cadastradas - nada foi inserido.';
        RETURN;
    END IF;

    SELECT id_cliente INTO c_ana    FROM cliente WHERE cpf = '529.982.247-25';
    SELECT id_cliente INTO c_carlos FROM cliente WHERE cpf = '168.995.350-09';
    SELECT id_cliente INTO c_ju     FROM cliente WHERE cpf = '390.533.447-05';

    IF c_ana IS NULL OR c_carlos IS NULL OR c_ju IS NULL THEN
        RAISE EXCEPTION 'Clientes de exemplo nao encontrados. Rode dados_exemplo.sql antes deste script.';
    END IF;

    SELECT id_produto INTO p1001 FROM produto WHERE codigo = 1001;
    SELECT id_produto INTO p1002 FROM produto WHERE codigo = 1002;
    SELECT id_produto INTO p1003 FROM produto WHERE codigo = 1003;
    SELECT id_produto INTO p1004 FROM produto WHERE codigo = 1004;
    SELECT id_produto INTO p1006 FROM produto WHERE codigo = 1006;

    IF p1001 IS NULL OR p1002 IS NULL OR p1003 IS NULL OR p1004 IS NULL OR p1006 IS NULL THEN
        RAISE EXCEPTION 'Produtos de exemplo nao encontrados. Rode dados_exemplo.sql antes deste script.';
    END IF;

    -- ---------- Venda 1: concluida, Pix ----------
    INSERT INTO venda (id_cliente, data, valor_total, status, observacao)
    VALUES (c_ana, CURRENT_TIMESTAMP - INTERVAL '18 days', 0, 'Concluída', 'Pix')
    RETURNING id_venda INTO v_id;
    INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) VALUES
        (v_id, p1001, 3, 12.90, 38.70),
        (v_id, p1002, 2, 14.90, 29.80);

    -- ---------- Venda 2: concluida, cartao de credito ----------
    INSERT INTO venda (id_cliente, data, valor_total, status, observacao)
    VALUES (c_carlos, CURRENT_TIMESTAMP - INTERVAL '12 days', 0, 'Concluída', 'Cartão de Crédito')
    RETURNING id_venda INTO v_id;
    INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) VALUES
        (v_id, p1003, 1, 39.90, 39.90),
        (v_id, p1004, 1, 29.90, 29.90);

    -- ---------- Venda 3: concluida, dinheiro (maior ticket) ----------
    INSERT INTO venda (id_cliente, data, valor_total, status, observacao)
    VALUES (c_ju, CURRENT_TIMESTAMP - INTERVAL '7 days', 0, 'Concluída', 'Dinheiro')
    RETURNING id_venda INTO v_id;
    INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) VALUES
        (v_id, p1001, 5, 12.90, 64.50),
        (v_id, p1006, 2, 24.90, 49.80);

    -- ---------- Venda 4: cancelada, Pix ----------
    INSERT INTO venda (id_cliente, data, valor_total, status, observacao)
    VALUES (c_ju, CURRENT_TIMESTAMP - INTERVAL '5 days', 0, 'Cancelada', 'Pix')
    RETURNING id_venda INTO v_id;
    INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) VALUES
        (v_id, p1003, 2, 39.90, 79.80);

    -- ---------- Venda 5: pendente, transferencia ----------
    INSERT INTO venda (id_cliente, data, valor_total, status, observacao)
    VALUES (c_ana, CURRENT_TIMESTAMP - INTERVAL '3 days', 0, 'Pendente', 'Transferência')
    RETURNING id_venda INTO v_id;
    INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) VALUES
        (v_id, p1006, 4, 24.90, 99.60);

    -- ---------- Venda 6: pendente, cartao de debito ----------
    INSERT INTO venda (id_cliente, data, valor_total, status, observacao)
    VALUES (c_carlos, CURRENT_TIMESTAMP - INTERVAL '1 day', 0, 'Pendente', 'Cartão de Débito')
    RETURNING id_venda INTO v_id;
    INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) VALUES
        (v_id, p1002, 2, 14.90, 29.80),
        (v_id, p1004, 1, 29.90, 29.90);

    -- Recalcula o total de cada venda a partir dos itens
    UPDATE venda v
       SET valor_total = COALESCE((SELECT SUM(i.subtotal) FROM item_venda i
                                    WHERE i.id_venda = v.id_venda), 0);

    -- Baixa o estoque dos produtos vendidos.
    -- Inclui as vendas canceladas de proposito: no sistema atual o
    -- cancelamento apenas muda o status e NAO devolve o estoque
    -- (VendaController.cancelarVenda), entao os dados ficam
    -- coerentes com o comportamento real do app.
    UPDATE produto p
       SET quantidade_estoque = GREATEST(0, p.quantidade_estoque - COALESCE((
               SELECT SUM(i.quantidade) FROM item_venda i WHERE i.id_produto = p.id_produto), 0));

    RAISE NOTICE 'Vendas de exemplo inseridas com sucesso.';
END $$;

-- ------------------------------------------------------------
-- Conferencia
-- ------------------------------------------------------------
SELECT v.id_venda,
       c.nome            AS cliente,
       v.data::date      AS data,
       v.valor_total,
       v.status,
       v.observacao      AS forma_pagamento,
       (SELECT COUNT(*) FROM item_venda i WHERE i.id_venda = v.id_venda) AS itens
  FROM venda v
  LEFT JOIN cliente c ON c.id_cliente = v.id_cliente
 ORDER BY v.data;
