package database;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.ItemVenda;
import model.Venda;

public class VendaDAO {

    /**
     * Insere uma nova venda junto com seus itens.
     * Também realiza a baixa do estoque na mesma transação.
     */
    public String inserir(Venda venda, List<ItemVenda> itens) {

        String mensagem = "Venda inserida com sucesso!";
        BD bd = new BD();

        try {

            bd.getConnection();
            bd.con.setAutoCommit(false);

            // ============================================================
            // 1. INSERIR VENDA
            // ============================================================

            String sqlVenda =
                    "INSERT INTO venda " +
                    "(id_cliente, data, valor_total, status, observacao) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "RETURNING id_venda";

            bd.st = bd.con.prepareStatement(sqlVenda);

            bd.st.setInt(
                    1,
                    venda.getIdCliente()
            );

            bd.st.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            venda.getData()
                    )
            );

            bd.st.setDouble(
                    3,
                    venda.getValorTotal()
            );

            bd.st.setString(
                    4,
                    venda.getStatus()
            );

            bd.st.setString(
                    5,
                    venda.getObservacao()
            );

            bd.rs = bd.st.executeQuery();

            int idVendaGerado;

            if (bd.rs.next()) {

                idVendaGerado =
                        bd.rs.getInt(
                                "id_venda"
                        );

                venda.setIdVenda(
                        idVendaGerado
                );

            } else {

                throw new SQLException(
                        "Não foi possível obter o ID da venda."
                );
            }


            // ============================================================
            // 2. INSERIR ITENS
            // ============================================================

            String sqlItem =
                    "INSERT INTO item_venda " +
                    "(id_venda, id_produto, quantidade, preco_unitario, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?)";

            bd.st = bd.con.prepareStatement(
                    sqlItem
            );

            for (ItemVenda item : itens) {

                bd.st.setInt(
                        1,
                        idVendaGerado
                );

                bd.st.setInt(
                        2,
                        item.getIdProduto()
                );

                bd.st.setInt(
                        3,
                        item.getQuantidade()
                );

                bd.st.setDouble(
                        4,
                        item.getPrecoUnitario()
                );

                bd.st.setDouble(
                        5,
                        item.getSubtotal()
                );

                bd.st.addBatch();
            }

            bd.st.executeBatch();


            // ============================================================
            // 3. BAIXAR ESTOQUE
            // ============================================================
            //
            // A condição:
            //
            // quantidade_estoque >= ?
            //
            // impede que o estoque fique negativo.
            //
            // Isso também protege o sistema quando dois computadores
            // tentam vender o mesmo produto ao mesmo tempo.
            // ============================================================

            String sqlEstoque =
                    "UPDATE produto " +
                    "SET quantidade_estoque = quantidade_estoque - ? " +
                    "WHERE id_produto = ? " +
                    "AND quantidade_estoque >= ?";

            bd.st = bd.con.prepareStatement(
                    sqlEstoque
            );

            for (ItemVenda item : itens) {

                bd.st.setInt(
                        1,
                        item.getQuantidade()
                );

                bd.st.setInt(
                        2,
                        item.getIdProduto()
                );

                bd.st.setInt(
                        3,
                        item.getQuantidade()
                );

                int linhasAfetadas =
                        bd.st.executeUpdate();

                if (linhasAfetadas == 0) {

                    throw new SQLException(
                            "Estoque insuficiente para o produto: "
                            + item.getNomeProduto()
                    );
                }
            }


            // ============================================================
            // 4. CONFIRMAR TRANSAÇÃO
            // ============================================================

            bd.con.commit();

            System.out.println(
                    "Venda inserida com sucesso - ID: "
                    + idVendaGerado
            );


        } catch (SQLException e) {

            mensagem =
                    "Falha ao inserir venda: "
                    + e.getMessage();

            e.printStackTrace();


            // ============================================================
            // ROLLBACK
            // ============================================================

            try {

                if (bd.con != null) {

                    bd.con.rollback();

                    System.out.println(
                            "Transação da venda revertida."
                    );
                }

            } catch (SQLException rollbackException) {

                System.err.println(
                        "Erro ao realizar rollback: "
                        + rollbackException.getMessage()
                );

                rollbackException.printStackTrace();
            }


        } finally {

            restaurarAutoCommit(bd);

            bd.close();
        }

        return mensagem;
    }


    /**
     * Atualiza os dados principais de uma venda.
     * Atualmente utilizado principalmente para alterar o status.
     */
    public String atualizar(Venda venda) {

        String mensagem =
                "Venda atualizada com sucesso!";

        BD bd = new BD();

        try {

            bd.getConnection();

            String sql =
                    "UPDATE venda " +
                    "SET status = ?, observacao = ?, valor_total = ? " +
                    "WHERE id_venda = ?";

            bd.st = bd.con.prepareStatement(
                    sql
            );

            bd.st.setString(
                    1,
                    venda.getStatus()
            );

            bd.st.setString(
                    2,
                    venda.getObservacao()
            );

            bd.st.setDouble(
                    3,
                    venda.getValorTotal()
            );

            bd.st.setInt(
                    4,
                    venda.getIdVenda()
            );

            int linhasAfetadas =
                    bd.st.executeUpdate();

            if (linhasAfetadas == 0) {

                mensagem =
                        "Venda não encontrada.";

            } else {

                System.out.println(
                        "Venda atualizada - ID: "
                        + venda.getIdVenda()
                );
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


    /**
     * Cancela uma venda pendente e devolve os produtos ao estoque.
     *
     * Toda a operação acontece dentro de uma única transação.
     */
    public String cancelar(int idVenda) {

        String mensagem =
                "Venda cancelada com sucesso!";

        BD bd = new BD();

        try {

            bd.getConnection();

            bd.con.setAutoCommit(false);


            // ============================================================
            // 1. ALTERAR STATUS
            // ============================================================
            //
            // Só permite cancelar vendas pendentes.
            //
            // Isso também impede que uma venda cancelada seja
            // cancelada novamente e devolva estoque em duplicidade.
            // ============================================================

            String sqlStatus =
                    "UPDATE venda " +
                    "SET status = 'Cancelada' " +
                    "WHERE id_venda = ? " +
                    "AND status = 'Pendente'";

            bd.st = bd.con.prepareStatement(
                    sqlStatus
            );

            bd.st.setInt(
                    1,
                    idVenda
            );

            int linhasAfetadas =
                    bd.st.executeUpdate();

            if (linhasAfetadas == 0) {

                bd.con.rollback();

                return "A venda não foi encontrada ou não está pendente.";
            }


            // ============================================================
            // 2. DEVOLVER PRODUTOS AO ESTOQUE
            // ============================================================

            String sqlEstoque =
                    "UPDATE produto p " +
                    "SET quantidade_estoque = " +
                    "p.quantidade_estoque + i.quantidade " +
                    "FROM item_venda i " +
                    "WHERE i.id_produto = p.id_produto " +
                    "AND i.id_venda = ?";

            bd.st = bd.con.prepareStatement(
                    sqlEstoque
            );

            bd.st.setInt(
                    1,
                    idVenda
            );

            bd.st.executeUpdate();


            // ============================================================
            // 3. COMMIT
            // ============================================================

            bd.con.commit();

            System.out.println(
                    "Venda cancelada e estoque devolvido - ID: "
                    + idVenda
            );


        } catch (SQLException e) {

            mensagem =
                    "Falha ao cancelar venda: "
                    + e.getMessage();

            e.printStackTrace();


            try {

                if (bd.con != null) {

                    bd.con.rollback();

                    System.out.println(
                            "Transação de cancelamento revertida."
                    );
                }

            } catch (SQLException rollbackException) {

                System.err.println(
                        "Erro ao realizar rollback: "
                        + rollbackException.getMessage()
                );

                rollbackException.printStackTrace();
            }


        } finally {

            restaurarAutoCommit(bd);

            bd.close();
        }

        return mensagem;
    }


    /**
     * Exclui uma venda.
     *
     * Caso a venda ainda tenha provocado baixa no estoque,
     * os produtos são devolvidos antes da exclusão.
     *
     * Se a venda já estiver cancelada, o estoque não é alterado,
     * pois o cancelar() já realizou a devolução.
     */
    public String deletar(int idVenda) {

        String mensagem =
                "Venda deletada com sucesso!";

        BD bd = new BD();

        try {

            bd.getConnection();

            bd.con.setAutoCommit(false);


            // ============================================================
            // 1. CONSULTAR STATUS
            // ============================================================

            String sqlBusca =
                    "SELECT status " +
                    "FROM venda " +
                    "WHERE id_venda = ?";

            bd.st = bd.con.prepareStatement(
                    sqlBusca
            );

            bd.st.setInt(
                    1,
                    idVenda
            );

            bd.rs = bd.st.executeQuery();


            if (!bd.rs.next()) {

                bd.con.rollback();

                return "Venda não encontrada.";
            }


            String status =
                    bd.rs.getString(
                            "status"
                    );


            // ============================================================
            // 2. DEVOLVER ESTOQUE SE NECESSÁRIO
            // ============================================================

            if (!"Cancelada".equals(status)) {

                String sqlEstoque =
                        "UPDATE produto p " +
                        "SET quantidade_estoque = " +
                        "p.quantidade_estoque + i.quantidade " +
                        "FROM item_venda i " +
                        "WHERE i.id_produto = p.id_produto " +
                        "AND i.id_venda = ?";

                bd.st = bd.con.prepareStatement(
                        sqlEstoque
                );

                bd.st.setInt(
                        1,
                        idVenda
                );

                bd.st.executeUpdate();
            }


            // ============================================================
            // 3. DELETAR VENDA
            // ============================================================
            //
            // Os itens podem ser apagados automaticamente caso exista
            // ON DELETE CASCADE na FK de item_venda.
            // ============================================================

            String sqlDelete =
                    "DELETE FROM venda " +
                    "WHERE id_venda = ?";

            bd.st = bd.con.prepareStatement(
                    sqlDelete
            );

            bd.st.setInt(
                    1,
                    idVenda
            );

            int linhasAfetadas =
                    bd.st.executeUpdate();


            if (linhasAfetadas == 0) {

                throw new SQLException(
                        "Não foi possível excluir a venda."
                );
            }


            bd.con.commit();

            System.out.println(
                    "Venda deletada - ID: "
                    + idVenda
            );


        } catch (SQLException e) {

            mensagem =
                    "Falha ao deletar venda: "
                    + e.getMessage();

            e.printStackTrace();


            try {

                if (bd.con != null) {

                    bd.con.rollback();

                    System.out.println(
                            "Exclusão da venda revertida."
                    );
                }

            } catch (SQLException rollbackException) {

                System.err.println(
                        "Erro ao realizar rollback: "
                        + rollbackException.getMessage()
                );

                rollbackException.printStackTrace();
            }


        } finally {

            restaurarAutoCommit(bd);

            bd.close();
        }

        return mensagem;
    }


    /**
     * Retorna todas as vendas com o nome do cliente.
     */
    public List<Venda> getAll() {

        List<Venda> lista =
                new ArrayList<>();

        BD bd = new BD();

        try {

            bd.getConnection();

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
                    "ORDER BY v.data DESC";

            bd.st = bd.con.prepareStatement(
                    sql
            );

            bd.rs = bd.st.executeQuery();


            while (bd.rs.next()) {

                int idVenda =
                        bd.rs.getInt(
                                "id_venda"
                        );

                int idCliente =
                        bd.rs.getInt(
                                "id_cliente"
                        );

                String nomeCliente =
                        bd.rs.getString(
                                "nome_cliente"
                        );

                Timestamp timestamp =
                        bd.rs.getTimestamp(
                                "data"
                        );

                double valorTotal =
                        bd.rs.getDouble(
                                "valor_total"
                        );

                String status =
                        bd.rs.getString(
                                "status"
                        );

                String observacao =
                        bd.rs.getString(
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

                lista.add(
                        venda
                );
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


    /**
     * Busca uma venda pelo ID.
     */
    public Venda buscarPorId(int idVenda) {

        Venda venda = null;

        BD bd = new BD();

        try {

            bd.getConnection();

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

            bd.st = bd.con.prepareStatement(
                    sql
            );

            bd.st.setInt(
                    1,
                    idVenda
            );

            bd.rs = bd.st.executeQuery();


            if (bd.rs.next()) {

                int idCliente =
                        bd.rs.getInt(
                                "id_cliente"
                        );

                String nomeCliente =
                        bd.rs.getString(
                                "nome_cliente"
                        );

                Timestamp timestamp =
                        bd.rs.getTimestamp(
                                "data"
                        );

                double valorTotal =
                        bd.rs.getDouble(
                                "valor_total"
                        );

                String status =
                        bd.rs.getString(
                                "status"
                        );

                String observacao =
                        bd.rs.getString(
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


    /**
     * Busca os itens de uma venda.
     */
    public List<ItemVenda> buscarItensPorVenda(
            int idVenda) {

        List<ItemVenda> lista =
                new ArrayList<>();

        BD bd = new BD();

        try {

            bd.getConnection();

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

            bd.st = bd.con.prepareStatement(
                    sql
            );

            bd.st.setInt(
                    1,
                    idVenda
            );

            bd.rs = bd.st.executeQuery();


            while (bd.rs.next()) {

                int idProduto =
                        bd.rs.getInt(
                                "id_produto"
                        );

                String nomeProduto =
                        bd.rs.getString(
                                "nome_produto"
                        );

                int quantidade =
                        bd.rs.getInt(
                                "quantidade"
                        );

                double precoUnitario =
                        bd.rs.getDouble(
                                "preco_unitario"
                        );

                double subtotal =
                        bd.rs.getDouble(
                                "subtotal"
                        );


                ItemVenda item =
                        new ItemVenda(
                                idVenda,
                                idProduto,
                                nomeProduto,
                                quantidade,
                                precoUnitario,
                                subtotal
                        );

                lista.add(
                        item
                );
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


    /**
     * Busca vendas pelo nome do cliente.
     */
    public List<Venda> buscarPorCliente(
            String nomeCliente) {

        List<Venda> lista =
                new ArrayList<>();

        BD bd = new BD();

        try {

            bd.getConnection();

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

            bd.st = bd.con.prepareStatement(
                    sql
            );

            bd.st.setString(
                    1,
                    "%"
                    + nomeCliente
                    + "%"
            );

            bd.rs = bd.st.executeQuery();


            while (bd.rs.next()) {

                int idVenda =
                        bd.rs.getInt(
                                "id_venda"
                        );

                int idCliente =
                        bd.rs.getInt(
                                "id_cliente"
                        );

                String nome =
                        bd.rs.getString(
                                "nome_cliente"
                        );

                Timestamp timestamp =
                        bd.rs.getTimestamp(
                                "data"
                        );

                double valorTotal =
                        bd.rs.getDouble(
                                "valor_total"
                        );

                String status =
                        bd.rs.getString(
                                "status"
                        );

                String observacao =
                        bd.rs.getString(
                                "observacao"
                        );


                Venda venda =
                        new Venda(
                                idVenda,
                                idCliente,
                                nome,
                                timestamp != null
                                        ? timestamp.toLocalDateTime()
                                        : null,
                                valorTotal,
                                status,
                                observacao
                        );

                lista.add(
                        venda
                );
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


    /**
     * Busca vendas pelo status.
     */
    public List<Venda> buscarPorStatus(
            String status) {

        List<Venda> lista =
                new ArrayList<>();

        BD bd = new BD();

        try {

            bd.getConnection();

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

            bd.st = bd.con.prepareStatement(
                    sql
            );

            bd.st.setString(
                    1,
                    status
            );

            bd.rs = bd.st.executeQuery();


            while (bd.rs.next()) {

                int idVenda =
                        bd.rs.getInt(
                                "id_venda"
                        );

                int idCliente =
                        bd.rs.getInt(
                                "id_cliente"
                        );

                String nomeCliente =
                        bd.rs.getString(
                                "nome_cliente"
                        );

                Timestamp timestamp =
                        bd.rs.getTimestamp(
                                "data"
                        );

                double valorTotal =
                        bd.rs.getDouble(
                                "valor_total"
                        );

                String statusVenda =
                        bd.rs.getString(
                                "status"
                        );

                String observacao =
                        bd.rs.getString(
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
                                statusVenda,
                                observacao
                        );

                lista.add(
                        venda
                );
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
    // AUXILIAR
    // ============================================================

    private void restaurarAutoCommit(BD bd) {

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