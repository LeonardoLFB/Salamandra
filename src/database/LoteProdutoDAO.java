package database;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.LoteProduto;

public class LoteProdutoDAO {

    private BD bd;


    public LoteProdutoDAO() {
        bd = new BD();
    }


    // ============================================================
    // INSERIR NOVO LOTE
    // ============================================================

    public boolean inserir(LoteProduto lote) {

        String sql =
                "INSERT INTO lote_produto " +
                "(id_produto, codigo_lote, quantidade, custo_unitario) " +
                "VALUES (?, ?, ?, ?)";

        try {

            if (!bd.getConnection()) {
                return false;
            }

            bd.st = bd.con.prepareStatement(sql);

            bd.st.setInt(
                    1,
                    lote.getIdProduto()
            );

            bd.st.setString(
                    2,
                    lote.getCodigoLote()
            );

            bd.st.setInt(
                    3,
                    lote.getQuantidade()
            );

            bd.st.setBigDecimal(
                    4,
                    lote.getCustoUnitario()
            );

            bd.st.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao inserir lote: "
                    + e.getMessage()
            );

            return false;

        } finally {

            bd.close();
        }
    }


    // ============================================================
    // BUSCAR LOTES DE UM PRODUTO
    // ============================================================

    public List<LoteProduto> buscarPorProduto(
            int idProduto) {

        List<LoteProduto> lotes =
                new ArrayList<>();

        String sql =
                "SELECT * " +
                "FROM lote_produto " +
                "WHERE id_produto = ? " +
                "ORDER BY data_entrada ASC, id ASC";

        try {

            if (!bd.getConnection()) {
                return lotes;
            }

            bd.st = bd.con.prepareStatement(sql);

            bd.st.setInt(
                    1,
                    idProduto
            );

            bd.rs = bd.st.executeQuery();

            while (bd.rs.next()) {

                LoteProduto lote =
                        new LoteProduto();

                lote.setId(
                        bd.rs.getInt("id")
                );

                lote.setIdProduto(
                        bd.rs.getInt("id_produto")
                );

                lote.setCodigoLote(
                        bd.rs.getString("codigo_lote")
                );

                lote.setQuantidade(
                        bd.rs.getInt("quantidade")
                );

                lote.setCustoUnitario(
                        bd.rs.getBigDecimal("custo_unitario")
                );

                lote.setDataEntrada(
                        bd.rs
                            .getTimestamp("data_entrada")
                            .toLocalDateTime()
                );

                lotes.add(lote);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao buscar lotes: "
                    + e.getMessage()
            );

        } finally {

            bd.close();
        }

        return lotes;
    }


    // ============================================================
    // SOMAR ESTOQUE DOS LOTES
    // ============================================================

    public int buscarQuantidadeTotal(
            int idProduto) {

        String sql =
                "SELECT COALESCE(SUM(quantidade), 0) AS total " +
                "FROM lote_produto " +
                "WHERE id_produto = ?";

        try {

            if (!bd.getConnection()) {
                return 0;
            }

            bd.st = bd.con.prepareStatement(sql);

            bd.st.setInt(
                    1,
                    idProduto
            );

            bd.rs = bd.st.executeQuery();

            if (bd.rs.next()) {

                return bd.rs.getInt(
                        "total"
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao calcular estoque dos lotes: "
                    + e.getMessage()
            );

        } finally {

            bd.close();
        }

        return 0;
    }
    
    public boolean registrarEntrada(LoteProduto lote) {

        String sqlLote =
                "INSERT INTO lote_produto " +
                "(id_produto, codigo_lote, quantidade, custo_unitario) " +
                "VALUES (?, ?, ?, ?)";

        String sqlProduto =
                "UPDATE produto " +
                "SET quantidade_estoque = quantidade_estoque + ? " +
                "WHERE id_produto = ?";

        try {

            if (!bd.getConnection()) {
                return false;
            }

            // Inicia a transação
            bd.con.setAutoCommit(false);


            // ============================================================
            // 1 - INSERE O NOVO LOTE
            // ============================================================

            bd.st = bd.con.prepareStatement(sqlLote);

            bd.st.setInt(
                    1,
                    lote.getIdProduto()
            );

            bd.st.setString(
                    2,
                    lote.getCodigoLote()
            );

            bd.st.setInt(
                    3,
                    lote.getQuantidade()
            );

            bd.st.setBigDecimal(
                    4,
                    lote.getCustoUnitario()
            );

            bd.st.executeUpdate();

            bd.st.close();


            // ============================================================
            // 2 - ATUALIZA O ESTOQUE TOTAL DO PRODUTO
            // ============================================================

            bd.st = bd.con.prepareStatement(sqlProduto);

            bd.st.setInt(
                    1,
                    lote.getQuantidade()
            );

            bd.st.setInt(
                    2,
                    lote.getIdProduto()
            );

            int linhasAtualizadas =
                    bd.st.executeUpdate();

            if (linhasAtualizadas == 0) {

                throw new SQLException(
                        "Produto não encontrado."
                );
            }


            // ============================================================
            // CONFIRMA TUDO
            // ============================================================

            bd.con.commit();

            return true;

        } catch (SQLException e) {

            try {

                if (bd.con != null) {
                    bd.con.rollback();
                }

            } catch (SQLException erroRollback) {

                erroRollback.printStackTrace();
            }

            System.out.println(
                    "Erro ao registrar entrada de estoque: "
                    + e.getMessage()
            );

            return false;

        } finally {

            try {

                if (bd.con != null) {
                    bd.con.setAutoCommit(true);
                }

            } catch (SQLException e) {
                // Nada a fazer
            }

            bd.close();
        }
    }
}