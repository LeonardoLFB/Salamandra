package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import application.SessaoUsuario;

import model.Auditoria;
import model.ClienteMaisComprou;
import model.ItemVenda;
import model.ProdutoMaisVendido;
import model.Usuario;
import model.Venda;

public class VendaDAO {

    // ============================================================
    // INSERIR VENDA
    // ============================================================

    public String inserir(Venda venda, List<ItemVenda> itens) {

        String mensagem = "Venda inserida com sucesso!";
        BD bd = new BD();

        try {

            Usuario usuarioLogado =
                    SessaoUsuario.getUsuarioLogado();

            if (usuarioLogado == null) {
                return "Nenhum usuário está logado no sistema.";
            }

            if (!bd.getConnection()) {
                return "Não foi possível conectar ao banco de dados.";
            }

            bd.con.setAutoCommit(false);

            // ========================================================
            // 1. INSERIR VENDA
            // ========================================================

            String sqlVenda =
                    "INSERT INTO venda " +
                    "(id_cliente, id_usuario, data, valor_total, status, observacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "RETURNING id_venda";

            int idVendaGerado;

            try (PreparedStatement stVenda =
                         bd.con.prepareStatement(sqlVenda)) {

                stVenda.setInt(
                        1,
                        venda.getIdCliente()
                );

                stVenda.setInt(
                        2,
                        usuarioLogado.getId()
                );

                stVenda.setTimestamp(
                        3,
                        Timestamp.valueOf(
                                venda.getData()
                        )
                );

                stVenda.setDouble(
                        4,
                        venda.getValorTotal()
                );

                stVenda.setString(
                        5,
                        venda.getStatus()
                );

                stVenda.setString(
                        6,
                        venda.getObservacao()
                );

                try (ResultSet rs =
                             stVenda.executeQuery()) {

                    if (!rs.next()) {

                        throw new SQLException(
                                "Não foi possível obter o ID da venda."
                        );
                    }

                    idVendaGerado =
                            rs.getInt(
                                    "id_venda"
                            );
                }
            }

            venda.setIdVenda(
                    idVendaGerado
            );

            venda.setIdUsuario(
                    usuarioLogado.getId()
            );

            venda.setNomeUsuario(
                    usuarioLogado.getNome()
            );


            // ========================================================
            // 2. INSERIR CADA ITEM E REALIZAR FIFO
            // ========================================================

            String sqlItem =
                    "INSERT INTO item_venda " +
                    "(id_venda, id_produto, quantidade, preco_unitario, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "RETURNING id_item";

            for (ItemVenda item : itens) {

                int idItemGerado;

                try (PreparedStatement stItem =
                             bd.con.prepareStatement(sqlItem)) {

                    stItem.setInt(
                            1,
                            idVendaGerado
                    );

                    stItem.setInt(
                            2,
                            item.getIdProduto()
                    );

                    stItem.setInt(
                            3,
                            item.getQuantidade()
                    );

                    stItem.setDouble(
                            4,
                            item.getPrecoUnitario()
                    );

                    stItem.setDouble(
                            5,
                            item.getSubtotal()
                    );

                    try (ResultSet rs =
                                 stItem.executeQuery()) {

                        if (!rs.next()) {

                            throw new SQLException(
                                    "Não foi possível obter o ID do item da venda."
                            );
                        }

                        idItemGerado =
                                rs.getInt(
                                        "id_item"
                                );
                    }
                }

                // ====================================================
                // BAIXA FIFO / PEPS
                // ====================================================

                baixarEstoqueFIFO(
                        bd,
                        item,
                        idItemGerado
                );
            }


            // ========================================================
            // 3. CONFIRMAR TRANSAÇÃO
            // ========================================================

            bd.con.commit();


            // ========================================================
            // 4. AUDITORIA
            // ========================================================

            Auditoria auditoria =
                    new Auditoria(
                            usuarioLogado.getId(),
                            "CADASTRO_VENDA",
                            "Cadastrou a venda #"
                                    + idVendaGerado
                    );

            new AuditoriaDAO().registrar(
                    auditoria
            );


            System.out.println(
                    "Venda inserida com sucesso - ID: "
                            + idVendaGerado
                            + " - Usuário: "
                            + usuarioLogado.getNome()
            );


        } catch (SQLException e) {

            mensagem =
                    "Falha ao inserir venda: "
                            + e.getMessage();

            e.printStackTrace();

            realizarRollback(
                    bd
            );

        } finally {

            restaurarAutoCommit(
                    bd
            );

            bd.close();
        }

        return mensagem;
    }


    // ============================================================
    // BAIXAR ESTOQUE FIFO / PEPS
    // ============================================================

    private void baixarEstoqueFIFO(
            BD bd,
            ItemVenda item,
            int idItem) throws SQLException {


        int quantidadeNecessaria =
                item.getQuantidade();


        if (quantidadeNecessaria <= 0) {

            throw new SQLException(
                    "Quantidade inválida para o produto: "
                            + item.getNomeProduto()
            );
        }


        // ========================================================
        // 1. VERIFICAR E BLOQUEAR O PRODUTO
        // ========================================================

        String sqlProduto =
                "SELECT quantidade_estoque " +
                "FROM produto " +
                "WHERE id_produto = ? " +
                "FOR UPDATE";


        int estoqueProduto;


        try (PreparedStatement st =
                     bd.con.prepareStatement(sqlProduto)) {

            st.setInt(
                    1,
                    item.getIdProduto()
            );


            try (ResultSet rs =
                         st.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Produto não encontrado: "
                                    + item.getNomeProduto()
                    );
                }


                estoqueProduto =
                        rs.getInt(
                                "quantidade_estoque"
                        );
            }
        }


        if (estoqueProduto
                < quantidadeNecessaria) {

            throw new SQLException(
                    "Estoque insuficiente para o produto: "
                            + item.getNomeProduto()
                            + ". Disponível: "
                            + estoqueProduto
                            + ", solicitado: "
                            + quantidadeNecessaria
            );
        }


        // ========================================================
        // 2. VERIFICAR ESTOQUE TOTAL NOS LOTES
        // ========================================================

        String sqlTotalLotes =
                "SELECT COALESCE(SUM(quantidade), 0) AS total " +
                "FROM lote_produto " +
                "WHERE id_produto = ?";


        int estoqueLotes;


        try (PreparedStatement st =
                     bd.con.prepareStatement(sqlTotalLotes)) {

            st.setInt(
                    1,
                    item.getIdProduto()
            );


            try (ResultSet rs =
                         st.executeQuery()) {

                rs.next();

                estoqueLotes =
                        rs.getInt(
                                "total"
                        );
            }
        }


        if (estoqueLotes
                < quantidadeNecessaria) {

            throw new SQLException(
                    "Estoque insuficiente nos lotes do produto: "
                            + item.getNomeProduto()
                            + ". Disponível nos lotes: "
                            + estoqueLotes
                            + ", solicitado: "
                            + quantidadeNecessaria
            );
        }


        // ========================================================
        // 3. BUSCAR LOTES MAIS ANTIGOS
        //
        // FOR UPDATE bloqueia os lotes durante esta venda.
        // ========================================================

        String sqlLotes =
                "SELECT " +
                "id, " +
                "codigo_lote, " +
                "quantidade, " +
                "custo_unitario " +
                "FROM lote_produto " +
                "WHERE id_produto = ? " +
                "AND quantidade > 0 " +
                "ORDER BY data_entrada ASC, id ASC " +
                "FOR UPDATE";


        class LoteFIFO {

            int id;
            String codigo;
            int quantidade;
            java.math.BigDecimal custo;

            LoteFIFO(
                    int id,
                    String codigo,
                    int quantidade,
                    java.math.BigDecimal custo) {

                this.id = id;
                this.codigo = codigo;
                this.quantidade = quantidade;
                this.custo = custo;
            }
        }


        List<LoteFIFO> lotes =
                new ArrayList<>();


        try (PreparedStatement st =
                     bd.con.prepareStatement(sqlLotes)) {

            st.setInt(
                    1,
                    item.getIdProduto()
            );


            try (ResultSet rs =
                         st.executeQuery()) {

                while (rs.next()) {

                    lotes.add(
                            new LoteFIFO(
                                    rs.getInt(
                                            "id"
                                    ),
                                    rs.getString(
                                            "codigo_lote"
                                    ),
                                    rs.getInt(
                                            "quantidade"
                                    ),
                                    rs.getBigDecimal(
                                            "custo_unitario"
                                    )
                            )
                    );
                }
            }
        }


        // ========================================================
        // 4. CONSUMIR LOTES
        // ========================================================

        int restante =
                quantidadeNecessaria;


        String sqlAtualizarLote =
                "UPDATE lote_produto " +
                "SET quantidade = quantidade - ? " +
                "WHERE id = ? " +
                "AND quantidade >= ?";


        String sqlHistorico =
                "INSERT INTO item_venda_lote " +
                "(id_item, id_lote_produto, quantidade, custo_unitario) " +
                "VALUES (?, ?, ?, ?)";


        for (LoteFIFO lote : lotes) {

            if (restante <= 0) {
                break;
            }


            int quantidadeRetirada =
                    Math.min(
                            restante,
                            lote.quantidade
                    );


            // ====================================================
            // DIMINUIR QUANTIDADE DO LOTE
            // ====================================================

            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlAtualizarLote
                         )) {

                st.setInt(
                        1,
                        quantidadeRetirada
                );

                st.setInt(
                        2,
                        lote.id
                );

                st.setInt(
                        3,
                        quantidadeRetirada
                );


                int linhas =
                        st.executeUpdate();


                if (linhas == 0) {

                    throw new SQLException(
                            "Não foi possível baixar o lote "
                                    + lote.codigo
                                    + "."
                    );
                }
            }


            // ====================================================
            // REGISTRAR QUAL LOTE FOI UTILIZADO
            // ====================================================

            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlHistorico
                         )) {

                st.setInt(
                        1,
                        idItem
                );

                st.setInt(
                        2,
                        lote.id
                );

                st.setInt(
                        3,
                        quantidadeRetirada
                );

                st.setBigDecimal(
                        4,
                        lote.custo
                );


                st.executeUpdate();
            }


            System.out.println(
                    "FIFO - Produto: "
                            + item.getNomeProduto()
                            + " | Lote: "
                            + lote.codigo
                            + " | Retirado: "
                            + quantidadeRetirada
                            + " | Custo: "
                            + lote.custo
            );


            restante -=
                    quantidadeRetirada;
        }


        if (restante > 0) {

            throw new SQLException(
                    "Não foi possível completar a baixa FIFO do produto: "
                            + item.getNomeProduto()
            );
        }


        // ========================================================
        // 5. ATUALIZAR ESTOQUE TOTAL DO PRODUTO
        // ========================================================

        String sqlAtualizarProduto =
                "UPDATE produto " +
                "SET quantidade_estoque = quantidade_estoque - ? " +
                "WHERE id_produto = ? " +
                "AND quantidade_estoque >= ?";


        try (PreparedStatement st =
                     bd.con.prepareStatement(
                             sqlAtualizarProduto
                     )) {

            st.setInt(
                    1,
                    quantidadeNecessaria
            );

            st.setInt(
                    2,
                    item.getIdProduto()
            );

            st.setInt(
                    3,
                    quantidadeNecessaria
            );


            int linhas =
                    st.executeUpdate();


            if (linhas == 0) {

                throw new SQLException(
                        "Não foi possível atualizar o estoque total do produto: "
                                + item.getNomeProduto()
                );
            }
        }
    }


    // ============================================================
    // ATUALIZAR / CONCLUIR VENDA
    // ============================================================

    public String atualizar(Venda venda) {

        String mensagem =
                "Venda atualizada com sucesso!";

        BD bd = new BD();

        try {

            if (!bd.getConnection()) {

                return "Não foi possível conectar ao banco de dados.";
            }


            String sql =
                    "UPDATE venda " +
                    "SET status = ?, observacao = ?, valor_total = ? " +
                    "WHERE id_venda = ?";


            try (PreparedStatement st =
                         bd.con.prepareStatement(sql)) {

                st.setString(
                        1,
                        venda.getStatus()
                );

                st.setString(
                        2,
                        venda.getObservacao()
                );

                st.setDouble(
                        3,
                        venda.getValorTotal()
                );

                st.setInt(
                        4,
                        venda.getIdVenda()
                );


                int linhasAfetadas =
                        st.executeUpdate();


                if (linhasAfetadas == 0) {

                    mensagem =
                            "Venda não encontrada.";

                } else {

                    System.out.println(
                            "Venda atualizada - ID: "
                                    + venda.getIdVenda()
                    );


                    if ("Concluída".equals(
                            venda.getStatus()
                    )) {

                        Usuario usuarioLogado =
                                SessaoUsuario
                                        .getUsuarioLogado();


                        if (usuarioLogado != null) {

                            Auditoria auditoria =
                                    new Auditoria(
                                            usuarioLogado.getId(),
                                            "CONCLUSAO_VENDA",
                                            "Concluiu a venda #"
                                                    + venda.getIdVenda()
                                    );


                            new AuditoriaDAO()
                                    .registrar(
                                            auditoria
                                    );
                        }
                    }
                }
            }


        } catch (SQLException e) {

            mensagem =
                    "Falha ao atualizar venda: "
                            + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // CANCELAR VENDA
    // ============================================================

    public String cancelar(
            int idVenda) {

        String mensagem =
                "Venda cancelada com sucesso!";


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return "Não foi possível conectar ao banco de dados.";
            }


            bd.con.setAutoCommit(
                    false
            );


            // ========================================================
            // 1. VERIFICAR VENDA
            // ========================================================

            String sqlVenda =
                    "SELECT status " +
                    "FROM venda " +
                    "WHERE id_venda = ? " +
                    "FOR UPDATE";


            String status;


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlVenda
                         )) {

                st.setInt(
                        1,
                        idVenda
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    if (!rs.next()) {

                        bd.con.rollback();

                        return "Venda não encontrada.";
                    }


                    status =
                            rs.getString(
                                    "status"
                            );
                }
            }


            if (!"Pendente".equals(
                    status
            )) {

                bd.con.rollback();

                return "A venda não está pendente.";
            }


            // ========================================================
            // 2. DEVOLVER ESTOQUE AOS LOTES
            // ========================================================

            devolverEstoqueDosLotes(
                    bd,
                    idVenda
            );


            // ========================================================
            // 3. ALTERAR STATUS
            // ========================================================

            String sqlStatus =
                    "UPDATE venda " +
                    "SET status = 'Cancelada' " +
                    "WHERE id_venda = ?";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlStatus
                         )) {

                st.setInt(
                        1,
                        idVenda
                );


                int linhas =
                        st.executeUpdate();


                if (linhas == 0) {

                    throw new SQLException(
                            "Não foi possível cancelar a venda."
                    );
                }
            }


            // ========================================================
            // 4. COMMIT
            // ========================================================

            bd.con.commit();


            // ========================================================
            // 5. AUDITORIA
            // ========================================================

            Usuario usuarioLogado =
                    SessaoUsuario
                            .getUsuarioLogado();


            if (usuarioLogado != null) {

                Auditoria auditoria =
                        new Auditoria(
                                usuarioLogado.getId(),
                                "CANCELAMENTO_VENDA",
                                "Cancelou a venda #"
                                        + idVenda
                        );


                new AuditoriaDAO()
                        .registrar(
                                auditoria
                        );
            }


            System.out.println(
                    "Venda cancelada e lotes restaurados - ID: "
                            + idVenda
            );


        } catch (SQLException e) {

            mensagem =
                    "Falha ao cancelar venda: "
                            + e.getMessage();


            e.printStackTrace();


            realizarRollback(
                    bd
            );

        } finally {

            restaurarAutoCommit(
                    bd
            );


            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // DEVOLVER ESTOQUE AOS LOTES
    // ============================================================

    private void devolverEstoqueDosLotes(
            BD bd,
            int idVenda) throws SQLException {


        // ========================================================
        // PRIMEIRO VERIFICAMOS SE TODOS OS ITENS POSSUEM
        // HISTÓRICO DE LOTE.
        //
        // Isso impede cancelamento incorreto de vendas antigas,
        // criadas antes da implementação do FIFO.
        // ========================================================

        String sqlVerificacao =
                "SELECT " +
                "iv.id_item, " +
                "iv.quantidade AS quantidade_vendida, " +
                "COALESCE(SUM(ivl.quantidade), 0) AS quantidade_lotes " +
                "FROM item_venda iv " +
                "LEFT JOIN item_venda_lote ivl " +
                "ON ivl.id_item = iv.id_item " +
                "WHERE iv.id_venda = ? " +
                "GROUP BY iv.id_item, iv.quantidade";


        try (PreparedStatement st =
                     bd.con.prepareStatement(
                             sqlVerificacao
                     )) {

            st.setInt(
                    1,
                    idVenda
            );


            try (ResultSet rs =
                         st.executeQuery()) {

                boolean encontrouItem =
                        false;


                while (rs.next()) {

                    encontrouItem =
                            true;


                    int quantidadeVendida =
                            rs.getInt(
                                    "quantidade_vendida"
                            );


                    int quantidadeLotes =
                            rs.getInt(
                                    "quantidade_lotes"
                            );


                    if (quantidadeVendida
                            != quantidadeLotes) {

                        throw new SQLException(
                                "Esta venda foi criada antes do controle de estoque por lotes "
                                        + "e não possui histórico FIFO completo. "
                                        + "O cancelamento foi bloqueado para evitar inconsistência no estoque."
                        );
                    }
                }


                if (!encontrouItem) {

                    throw new SQLException(
                            "A venda não possui itens."
                    );
                }
            }
        }


        // ========================================================
        // BUSCAR LOTES UTILIZADOS
        // ========================================================

        String sqlHistorico =
                "SELECT " +
                "iv.id_produto, " +
                "ivl.id_lote_produto, " +
                "ivl.quantidade " +
                "FROM item_venda iv " +
                "INNER JOIN item_venda_lote ivl " +
                "ON ivl.id_item = iv.id_item " +
                "WHERE iv.id_venda = ? " +
                "ORDER BY ivl.id";


        class DevolucaoLote {

            int idProduto;
            int idLote;
            int quantidade;

            DevolucaoLote(
                    int idProduto,
                    int idLote,
                    int quantidade) {

                this.idProduto =
                        idProduto;

                this.idLote =
                        idLote;

                this.quantidade =
                        quantidade;
            }
        }


        List<DevolucaoLote> devolucoes =
                new ArrayList<>();


        try (PreparedStatement st =
                     bd.con.prepareStatement(
                             sqlHistorico
                     )) {

            st.setInt(
                    1,
                    idVenda
            );


            try (ResultSet rs =
                         st.executeQuery()) {

                while (rs.next()) {

                    devolucoes.add(
                            new DevolucaoLote(
                                    rs.getInt(
                                            "id_produto"
                                    ),
                                    rs.getInt(
                                            "id_lote_produto"
                                    ),
                                    rs.getInt(
                                            "quantidade"
                                    )
                            )
                    );
                }
            }
        }


        // ========================================================
        // DEVOLVER PARA CADA LOTE
        // ========================================================

        String sqlLote =
                "UPDATE lote_produto " +
                "SET quantidade = quantidade + ? " +
                "WHERE id = ?";


        for (DevolucaoLote devolucao :
                devolucoes) {


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlLote
                         )) {

                st.setInt(
                        1,
                        devolucao.quantidade
                );

                st.setInt(
                        2,
                        devolucao.idLote
                );


                int linhas =
                        st.executeUpdate();


                if (linhas == 0) {

                    throw new SQLException(
                            "Lote não encontrado durante a devolução."
                    );
                }
            }
        }


        // ========================================================
        // DEVOLVER AO ESTOQUE TOTAL DO PRODUTO
        // ========================================================

        String sqlProdutos =
                "SELECT " +
                "id_produto, " +
                "SUM(quantidade) AS quantidade " +
                "FROM item_venda " +
                "WHERE id_venda = ? " +
                "GROUP BY id_produto";


        class DevolucaoProduto {

            int idProduto;
            int quantidade;

            DevolucaoProduto(
                    int idProduto,
                    int quantidade) {

                this.idProduto =
                        idProduto;

                this.quantidade =
                        quantidade;
            }
        }


        List<DevolucaoProduto> produtos =
                new ArrayList<>();


        try (PreparedStatement st =
                     bd.con.prepareStatement(
                             sqlProdutos
                     )) {

            st.setInt(
                    1,
                    idVenda
            );


            try (ResultSet rs =
                         st.executeQuery()) {

                while (rs.next()) {

                    produtos.add(
                            new DevolucaoProduto(
                                    rs.getInt(
                                            "id_produto"
                                    ),
                                    rs.getInt(
                                            "quantidade"
                                    )
                            )
                    );
                }
            }
        }


        String sqlProduto =
                "UPDATE produto " +
                "SET quantidade_estoque = quantidade_estoque + ? " +
                "WHERE id_produto = ?";


        for (DevolucaoProduto produto :
                produtos) {


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlProduto
                         )) {

                st.setInt(
                        1,
                        produto.quantidade
                );

                st.setInt(
                        2,
                        produto.idProduto
                );


                int linhas =
                        st.executeUpdate();


                if (linhas == 0) {

                    throw new SQLException(
                            "Produto não encontrado durante a devolução."
                    );
                }
            }
        }
    }


    // ============================================================
    // EXCLUIR VENDA
    // ============================================================

    public String deletar(
            int idVenda) {

        String mensagem =
                "Venda deletada com sucesso!";


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return "Não foi possível conectar ao banco de dados.";
            }


            bd.con.setAutoCommit(
                    false
            );


            // ========================================================
            // 1. CONSULTAR STATUS
            // ========================================================

            String sqlBusca =
                    "SELECT status " +
                    "FROM venda " +
                    "WHERE id_venda = ? " +
                    "FOR UPDATE";


            String status;


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlBusca
                         )) {

                st.setInt(
                        1,
                        idVenda
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    if (!rs.next()) {

                        bd.con.rollback();

                        return "Venda não encontrada.";
                    }


                    status =
                            rs.getString(
                                    "status"
                            );
                }
            }


            // ========================================================
            // 2. SE NÃO ESTIVER CANCELADA, DEVOLVER ESTOQUE
            // ========================================================

            if (!"Cancelada".equals(
                    status
            )) {

                devolverEstoqueDosLotes(
                        bd,
                        idVenda
                );
            }


            // ========================================================
            // 3. EXCLUIR VENDA
            // ========================================================

            String sqlDelete =
                    "DELETE FROM venda " +
                    "WHERE id_venda = ?";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sqlDelete
                         )) {

                st.setInt(
                        1,
                        idVenda
                );


                int linhasAfetadas =
                        st.executeUpdate();


                if (linhasAfetadas == 0) {

                    throw new SQLException(
                            "Não foi possível excluir a venda."
                    );
                }
            }


            // item_venda_lote possui ON DELETE CASCADE
            // através de item_venda.
            //
            // Portanto, ao excluir os itens da venda,
            // o histórico dos lotes também será removido.


            // ========================================================
            // 4. COMMIT
            // ========================================================

            bd.con.commit();


            // ========================================================
            // 5. AUDITORIA
            // ========================================================

            Usuario usuarioLogado =
                    SessaoUsuario
                            .getUsuarioLogado();


            if (usuarioLogado != null) {

                Auditoria auditoria =
                        new Auditoria(
                                usuarioLogado.getId(),
                                "EXCLUSAO_VENDA",
                                "Excluiu a venda #"
                                        + idVenda
                        );


                new AuditoriaDAO()
                        .registrar(
                                auditoria
                        );
            }


            System.out.println(
                    "Venda deletada - ID: "
                            + idVenda
            );


        } catch (SQLException e) {

            mensagem =
                    "Falha ao deletar venda: "
                            + e.getMessage();


            e.printStackTrace();


            realizarRollback(
                    bd
            );

        } finally {

            restaurarAutoCommit(
                    bd
            );


            bd.close();
        }


        return mensagem;
    }


    // ============================================================
    // LISTAR TODAS AS VENDAS
    // ============================================================

    public List<Venda> getAll() {

        List<Venda> lista =
                new ArrayList<>();

        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "v.id_venda, " +
                    "v.id_cliente, " +
                    "v.id_usuario, " +
                    "c.nome AS nome_cliente, " +
                    "u.nome AS nome_usuario, " +
                    "v.data, " +
                    "v.valor_total, " +
                    "v.status, " +
                    "v.observacao " +
                    "FROM venda v " +
                    "LEFT JOIN cliente c " +
                    "ON v.id_cliente = c.id_cliente " +
                    "LEFT JOIN usuario u " +
                    "ON v.id_usuario = u.id_usuario " +
                    "ORDER BY v.data DESC";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         );

                 ResultSet rs =
                         st.executeQuery()) {


                while (rs.next()) {

                    int idVenda =
                            rs.getInt(
                                    "id_venda"
                            );


                    int idCliente =
                            rs.getInt(
                                    "id_cliente"
                            );


                    int idUsuario =
                            rs.getInt(
                                    "id_usuario"
                            );


                    String nomeCliente =
                            rs.getString(
                                    "nome_cliente"
                            );


                    String nomeUsuario =
                            rs.getString(
                                    "nome_usuario"
                            );


                    Timestamp timestamp =
                            rs.getTimestamp(
                                    "data"
                            );


                    double valorTotal =
                            rs.getDouble(
                                    "valor_total"
                            );


                    String status =
                            rs.getString(
                                    "status"
                            );


                    String observacao =
                            rs.getString(
                                    "observacao"
                            );


                    Venda venda =
                            new Venda(
                                    idVenda,
                                    idCliente,
                                    nomeCliente,
                                    timestamp != null
                                            ? timestamp.toLocalDateTime()
                                            : null,
                                    valorTotal,
                                    status,
                                    observacao
                            );


                    venda.setIdUsuario(
                            idUsuario
                    );


                    venda.setNomeUsuario(
                            nomeUsuario
                    );


                    lista.add(
                            venda
                    );
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro em VendaDAO.getAll(): "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        System.out.println(
                "VendaDAO.getAll() retornou "
                        + lista.size()
                        + " registros."
        );


        return lista;
    }


    // ============================================================
    // BUSCAR VENDA POR ID
    // ============================================================

    public Venda buscarPorId(
            int idVenda) {

        Venda venda =
                null;


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return null;
            }


            String sql =
                    "SELECT " +
                    "v.id_venda, " +
                    "v.id_cliente, " +
                    "c.nome AS nome_cliente, " +
                    "v.data, " +
                    "v.valor_total, " +
                    "v.status, " +
                    "v.observacao " +
                    "FROM venda v " +
                    "LEFT JOIN cliente c " +
                    "ON v.id_cliente = c.id_cliente " +
                    "WHERE v.id_venda = ?";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setInt(
                        1,
                        idVenda
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    if (rs.next()) {

                        int idCliente =
                                rs.getInt(
                                        "id_cliente"
                                );


                        String nomeCliente =
                                rs.getString(
                                        "nome_cliente"
                                );


                        Timestamp timestamp =
                                rs.getTimestamp(
                                        "data"
                                );


                        double valorTotal =
                                rs.getDouble(
                                        "valor_total"
                                );


                        String status =
                                rs.getString(
                                        "status"
                                );


                        String observacao =
                                rs.getString(
                                        "observacao"
                                );


                        venda =
                                new Venda(
                                        idVenda,
                                        idCliente,
                                        nomeCliente,
                                        timestamp != null
                                                ? timestamp.toLocalDateTime()
                                                : null,
                                        valorTotal,
                                        status,
                                        observacao
                                );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar venda por ID: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return venda;
    }


    // ============================================================
    // BUSCAR ITENS DA VENDA
    // ============================================================

    public List<ItemVenda> buscarItensPorVenda(
            int idVenda) {

        List<ItemVenda> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "iv.id_venda, " +
                    "iv.id_produto, " +
                    "p.nome AS nome_produto, " +
                    "iv.quantidade, " +
                    "iv.preco_unitario, " +
                    "iv.subtotal " +
                    "FROM item_venda iv " +
                    "JOIN produto p " +
                    "ON iv.id_produto = p.id_produto " +
                    "WHERE iv.id_venda = ?";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setInt(
                        1,
                        idVenda
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    while (rs.next()) {

                        ItemVenda item =
                                new ItemVenda(
                                        idVenda,
                                        rs.getInt(
                                                "id_produto"
                                        ),
                                        rs.getString(
                                                "nome_produto"
                                        ),
                                        rs.getInt(
                                                "quantidade"
                                        ),
                                        rs.getDouble(
                                                "preco_unitario"
                                        ),
                                        rs.getDouble(
                                                "subtotal"
                                        )
                                );


                        lista.add(
                                item
                        );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar itens da venda: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return lista;
    }


    // ============================================================
    // BUSCAR POR CLIENTE
    // ============================================================

    public List<Venda> buscarPorCliente(
            String nomeCliente) {

        List<Venda> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "v.id_venda, " +
                    "v.id_cliente, " +
                    "c.nome AS nome_cliente, " +
                    "v.data, " +
                    "v.valor_total, " +
                    "v.status, " +
                    "v.observacao " +
                    "FROM venda v " +
                    "LEFT JOIN cliente c " +
                    "ON v.id_cliente = c.id_cliente " +
                    "WHERE LOWER(c.nome) LIKE LOWER(?) " +
                    "ORDER BY v.data DESC";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setString(
                        1,
                        "%"
                                + nomeCliente
                                + "%"
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    while (rs.next()) {

                        Venda venda =
                                new Venda(
                                        rs.getInt(
                                                "id_venda"
                                        ),
                                        rs.getInt(
                                                "id_cliente"
                                        ),
                                        rs.getString(
                                                "nome_cliente"
                                        ),
                                        rs.getTimestamp(
                                                "data"
                                        ) != null
                                                ? rs.getTimestamp(
                                                        "data"
                                                ).toLocalDateTime()
                                                : null,
                                        rs.getDouble(
                                                "valor_total"
                                        ),
                                        rs.getString(
                                                "status"
                                        ),
                                        rs.getString(
                                                "observacao"
                                        )
                                );


                        lista.add(
                                venda
                        );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar vendas por cliente: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return lista;
    }


    // ============================================================
    // BUSCAR POR STATUS
    // ============================================================

    public List<Venda> buscarPorStatus(
            String status) {

        List<Venda> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "v.id_venda, " +
                    "v.id_cliente, " +
                    "c.nome AS nome_cliente, " +
                    "v.data, " +
                    "v.valor_total, " +
                    "v.status, " +
                    "v.observacao " +
                    "FROM venda v " +
                    "LEFT JOIN cliente c " +
                    "ON v.id_cliente = c.id_cliente " +
                    "WHERE v.status = ? " +
                    "ORDER BY v.data DESC";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setString(
                        1,
                        status
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    while (rs.next()) {

                        Timestamp timestamp =
                                rs.getTimestamp(
                                        "data"
                                );


                        Venda venda =
                                new Venda(
                                        rs.getInt(
                                                "id_venda"
                                        ),
                                        rs.getInt(
                                                "id_cliente"
                                        ),
                                        rs.getString(
                                                "nome_cliente"
                                        ),
                                        timestamp != null
                                                ? timestamp.toLocalDateTime()
                                                : null,
                                        rs.getDouble(
                                                "valor_total"
                                        ),
                                        rs.getString(
                                                "status"
                                        ),
                                        rs.getString(
                                                "observacao"
                                        )
                                );


                        lista.add(
                                venda
                        );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar vendas por status: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return lista;
    }


    // ============================================================
    // BUSCAR POR PERÍODO
    // ============================================================

    public List<Venda> buscarPorPeriodo(
            LocalDateTime dataInicial,
            LocalDateTime dataFinal) {

        List<Venda> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "v.id_venda, " +
                    "v.id_cliente, " +
                    "c.nome AS nome_cliente, " +
                    "v.data, " +
                    "v.valor_total, " +
                    "v.status, " +
                    "v.observacao " +
                    "FROM venda v " +
                    "LEFT JOIN cliente c " +
                    "ON v.id_cliente = c.id_cliente " +
                    "WHERE v.data BETWEEN ? AND ? " +
                    "ORDER BY v.data DESC";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setTimestamp(
                        1,
                        Timestamp.valueOf(
                                dataInicial
                        )
                );


                st.setTimestamp(
                        2,
                        Timestamp.valueOf(
                                dataFinal
                        )
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    while (rs.next()) {

                        Timestamp timestamp =
                                rs.getTimestamp(
                                        "data"
                                );


                        Venda venda =
                                new Venda(
                                        rs.getInt(
                                                "id_venda"
                                        ),
                                        rs.getInt(
                                                "id_cliente"
                                        ),
                                        rs.getString(
                                                "nome_cliente"
                                        ),
                                        timestamp != null
                                                ? timestamp.toLocalDateTime()
                                                : null,
                                        rs.getDouble(
                                                "valor_total"
                                        ),
                                        rs.getString(
                                                "status"
                                        ),
                                        rs.getString(
                                                "observacao"
                                        )
                                );


                        lista.add(
                                venda
                        );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar vendas por período: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return lista;
    }


    // ============================================================
    // PRODUTOS MAIS VENDIDOS
    // ============================================================

    public List<ProdutoMaisVendido> buscarProdutosMaisVendidos(
            LocalDateTime dataInicial,
            LocalDateTime dataFinal) {

        List<ProdutoMaisVendido> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "p.id_produto, " +
                    "p.nome AS nome_produto, " +
                    "SUM(iv.quantidade) AS quantidade_vendida, " +
                    "SUM(iv.subtotal) AS valor_total " +
                    "FROM item_venda iv " +
                    "INNER JOIN produto p " +
                    "ON iv.id_produto = p.id_produto " +
                    "INNER JOIN venda v " +
                    "ON iv.id_venda = v.id_venda " +
                    "WHERE v.data BETWEEN ? AND ? " +
                    "AND LOWER(v.status) <> 'cancelada' " +
                    "GROUP BY p.id_produto, p.nome " +
                    "ORDER BY quantidade_vendida DESC";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setTimestamp(
                        1,
                        Timestamp.valueOf(
                                dataInicial
                        )
                );


                st.setTimestamp(
                        2,
                        Timestamp.valueOf(
                                dataFinal
                        )
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    while (rs.next()) {

                        ProdutoMaisVendido produto =
                                new ProdutoMaisVendido(
                                        rs.getInt(
                                                "id_produto"
                                        ),
                                        rs.getString(
                                                "nome_produto"
                                        ),
                                        rs.getInt(
                                                "quantidade_vendida"
                                        ),
                                        rs.getDouble(
                                                "valor_total"
                                        )
                                );


                        lista.add(
                                produto
                        );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar produtos mais vendidos: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return lista;
    }


    // ============================================================
    // CLIENTES QUE MAIS COMPRARAM
    // ============================================================

    public List<ClienteMaisComprou> buscarClientesQueMaisCompraram(
            LocalDateTime dataInicial,
            LocalDateTime dataFinal) {

        List<ClienteMaisComprou> lista =
                new ArrayList<>();


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return lista;
            }


            String sql =
                    "SELECT " +
                    "c.id_cliente, " +
                    "c.nome AS nome_cliente, " +
                    "COUNT(v.id_venda) AS quantidade_compras, " +
                    "SUM(v.valor_total) AS valor_total " +
                    "FROM venda v " +
                    "INNER JOIN cliente c " +
                    "ON v.id_cliente = c.id_cliente " +
                    "WHERE v.data BETWEEN ? AND ? " +
                    "AND LOWER(v.status) = LOWER('Concluída') " +
                    "GROUP BY c.id_cliente, c.nome " +
                    "ORDER BY valor_total DESC";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setTimestamp(
                        1,
                        Timestamp.valueOf(
                                dataInicial
                        )
                );


                st.setTimestamp(
                        2,
                        Timestamp.valueOf(
                                dataFinal
                        )
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    while (rs.next()) {

                        ClienteMaisComprou cliente =
                                new ClienteMaisComprou(
                                        rs.getInt(
                                                "id_cliente"
                                        ),
                                        rs.getString(
                                                "nome_cliente"
                                        ),
                                        rs.getInt(
                                                "quantidade_compras"
                                        ),
                                        rs.getDouble(
                                                "valor_total"
                                        )
                                );


                        lista.add(
                                cliente
                        );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar clientes que mais compraram: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return lista;
    }


    // ============================================================
    // CONTAR VENDAS POR USUÁRIO
    // ============================================================

    public int contarVendasPorUsuario(
            int idUsuario) {

        int quantidade =
                0;


        BD bd =
                new BD();


        try {

            if (!bd.getConnection()) {

                return 0;
            }


            String sql =
                    "SELECT COUNT(*) AS quantidade " +
                    "FROM venda " +
                    "WHERE id_usuario = ? " +
                    "AND status = 'Concluída'";


            try (PreparedStatement st =
                         bd.con.prepareStatement(
                                 sql
                         )) {

                st.setInt(
                        1,
                        idUsuario
                );


                try (ResultSet rs =
                             st.executeQuery()) {

                    if (rs.next()) {

                        quantidade =
                                rs.getInt(
                                        "quantidade"
                                );
                    }
                }
            }


        } catch (SQLException e) {

            System.err.println(
                    "Erro ao contar vendas do usuário: "
                            + e.getMessage()
            );


            e.printStackTrace();

        } finally {

            bd.close();
        }


        return quantidade;
    }


    // ============================================================
    // ROLLBACK
    // ============================================================

    private void realizarRollback(
            BD bd) {

        try {

            if (bd.con != null) {

                bd.con.rollback();


                System.out.println(
                        "Transação revertida."
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao realizar rollback: "
                            + e.getMessage()
            );


            e.printStackTrace();
        }
    }


    // ============================================================
    // RESTAURAR AUTO-COMMIT
    // ============================================================

    private void restaurarAutoCommit(
            BD bd) {

        try {

            if (bd.con != null) {

                bd.con.setAutoCommit(
                        true
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao restaurar auto-commit: "
                            + e.getMessage()
            );


            e.printStackTrace();
        }
    }
}