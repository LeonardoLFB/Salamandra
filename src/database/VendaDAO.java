package database;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.ItemVenda;
import model.Venda;

public class VendaDAO {
    
    /**
     * Insere uma nova venda no banco de dados junto com seus itens
     * @param venda - objeto Venda preenchido
     * @param itens - lista de ItemVenda
     * @return mensagem de sucesso ou erro
     */
    public String inserir(Venda venda, List<ItemVenda> itens) {
        String s = "Venda inserida com sucesso!";
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            // Desabilita auto-commit para fazer transação
            bd.con.setAutoCommit(false);
            
            // 1. Inserir a venda
            String sqlVenda = "INSERT INTO venda (id_cliente, data, valor_total, status, observacao) " +
                             "VALUES (?, ?, ?, ?, ?) RETURNING id_venda";
            
            bd.st = bd.con.prepareStatement(sqlVenda);
            bd.st.setInt(1, venda.getIdCliente());
            bd.st.setTimestamp(2, Timestamp.valueOf(venda.getData()));
            bd.st.setDouble(3, venda.getValorTotal());
            bd.st.setString(4, venda.getStatus());
            bd.st.setString(5, venda.getObservacao());
            
            bd.rs = bd.st.executeQuery();
            
            int idVendaGerado = 0;
            if (bd.rs.next()) {
                idVendaGerado = bd.rs.getInt(1);
                venda.setIdVenda(idVendaGerado);
            } else {
                throw new SQLException("Falha ao obter ID da venda gerada.");
            }
            
            // 2. Inserir os itens da venda
            String sqlItem = "INSERT INTO item_venda (id_venda, id_produto, quantidade, preco_unitario, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?)";
            
            bd.st = bd.con.prepareStatement(sqlItem);
            
            for (ItemVenda item : itens) {
                bd.st.setInt(1, idVendaGerado);
                bd.st.setInt(2, item.getIdProduto());
                bd.st.setInt(3, item.getQuantidade());
                bd.st.setDouble(4, item.getPrecoUnitario());
                bd.st.setDouble(5, item.getSubtotal());
                bd.st.addBatch();
            }
            
            bd.st.executeBatch();
            
            // 3. Atualizar estoque dos produtos
            String sqlUpdateEstoque = "UPDATE produto SET quantidade_estoque = quantidade_estoque - ? " +
                                     "WHERE id_produto = ?";
            
            bd.st = bd.con.prepareStatement(sqlUpdateEstoque);
            
            for (ItemVenda item : itens) {
                bd.st.setInt(1, item.getQuantidade());
                bd.st.setInt(2, item.getIdProduto());
                bd.st.addBatch();
            }
            
            bd.st.executeBatch();
            
            // Commit da transação
            bd.con.commit();
            
            System.out.println("Venda inserida com sucesso - ID: " + idVendaGerado);
            
        } catch (SQLException e) {
            s = "Falha ao inserir venda: " + e.getMessage();
            e.printStackTrace();
            
            try {
                if (bd.con != null) {
                    bd.con.rollback(); // Desfaz tudo em caso de erro
                    System.out.println("Transação revertida devido a erro.");
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
        } finally {
            try {
                if (bd.con != null) {
                    bd.con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Erro ao restaurar auto-commit: " + e.getMessage());
                e.printStackTrace();
            }
            bd.close();
        }
        
        return s;
    }
    
    /**
     * Atualiza uma venda existente
     * @param venda
     * @return mensagem de sucesso ou erro
     */
    public String atualizar(Venda venda) {
        String s = "Venda atualizada com sucesso!";
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "UPDATE venda SET status = ?, observacao = ?, valor_total = ? WHERE id_venda = ?";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setString(1, venda.getStatus());
            bd.st.setString(2, venda.getObservacao());
            bd.st.setDouble(3, venda.getValorTotal());
            bd.st.setInt(4, venda.getIdVenda());
            
            int n = bd.st.executeUpdate();
            
            if (n == 0) {
                s = "Venda não encontrada.";
            } else {
                System.out.println("Venda atualizada - ID: " + venda.getIdVenda());
            }
            
        } catch (SQLException e) {
            s = "Falha ao atualizar venda: " + e.getMessage();
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        return s;
    }
    
    /**
     * Cancela uma venda e devolve ao estoque os produtos dos seus itens.
     * A operação é transacional: ou o status muda e o estoque volta, ou nada acontece.
     * @param idVenda
     * @return mensagem de sucesso ou erro
     */
    public String cancelar(int idVenda) {
        String s = "Venda cancelada com sucesso!";
        BD bd = new BD();

        try {
            bd.getConnection();

            bd.con.setAutoCommit(false);

            // 1. Marca como cancelada somente se ainda não estiver.
            //    A condição dentro do próprio UPDATE garante que uma venda
            //    cancelada duas vezes não devolva o estoque em duplicidade.
            String sqlStatus = "UPDATE venda SET status = 'Cancelada' " +
                               "WHERE id_venda = ? AND status <> 'Cancelada'";

            bd.st = bd.con.prepareStatement(sqlStatus);
            bd.st.setInt(1, idVenda);

            int n = bd.st.executeUpdate();

            if (n == 0) {
                bd.con.rollback();
                return "Venda não encontrada ou já cancelada.";
            }

            // 2. Devolve ao estoque a quantidade de cada item da venda
            String sqlEstoque = "UPDATE produto p " +
                                "SET quantidade_estoque = p.quantidade_estoque + i.quantidade " +
                                "FROM item_venda i " +
                                "WHERE i.id_produto = p.id_produto AND i.id_venda = ?";

            bd.st = bd.con.prepareStatement(sqlEstoque);
            bd.st.setInt(1, idVenda);
            bd.st.executeUpdate();

            bd.con.commit();

            System.out.println("Venda cancelada e estoque devolvido - ID: " + idVenda);

        } catch (SQLException e) {
            s = "Falha ao cancelar venda: " + e.getMessage();
            e.printStackTrace();

            try {
                if (bd.con != null) {
                    bd.con.rollback();
                    System.out.println("Transação revertida devido a erro.");
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
        } finally {
            try {
                if (bd.con != null) {
                    bd.con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Erro ao restaurar auto-commit: " + e.getMessage());
                e.printStackTrace();
            }
            bd.close();
        }

        return s;
    }

    /**
     * Deleta uma venda (CASCADE deleta os itens automaticamente)
     * @param idVenda
     * @return mensagem de sucesso ou erro
     */
    public String deletar(int idVenda) {
        String s = "Venda deletada com sucesso!";
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "DELETE FROM venda WHERE id_venda = ? RETURNING id_venda";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setInt(1, idVenda);
            
            bd.rs = bd.st.executeQuery();
            
            if (!bd.rs.next()) {
                s = "Venda não encontrada ou não foi possível deletar.";
            } else {
                int deletedId = bd.rs.getInt(1);
                System.out.println("Venda deletada - ID: " + deletedId);
            }
            
        } catch (SQLException e) {
            s = "Falha ao deletar venda: " + e.getMessage();
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        return s;
    }
    
    /**
     * Retorna todas as vendas com o nome do cliente
     * @return lista de vendas
     */
    public List<Venda> getAll() {
        List<Venda> lista = new ArrayList<>();
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "SELECT v.id_venda, v.id_cliente, c.nome AS nome_cliente, v.data, " +
                         "v.valor_total, v.status, v.observacao " +
                         "FROM venda v " +
                         "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                         "ORDER BY v.data DESC";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.rs = bd.st.executeQuery();
            
            while (bd.rs.next()) {
                int idVenda = bd.rs.getInt("id_venda");
                int idCliente = bd.rs.getInt("id_cliente");
                String nomeCliente = bd.rs.getString("nome_cliente");
                Timestamp ts = bd.rs.getTimestamp("data");
                double valorTotal = bd.rs.getDouble("valor_total");
                String status = bd.rs.getString("status");
                String observacao = bd.rs.getString("observacao");
                
                Venda v = new Venda(idVenda, idCliente, nomeCliente, 
                                   ts != null ? ts.toLocalDateTime() : null, 
                                   valorTotal, status, observacao);
                lista.add(v);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro em VendaDAO.getAll(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        System.out.println("VendaDAO.getAll() retornou " + lista.size() + " registros.");
        return lista;
    }
    
    /**
     * Busca uma venda por ID
     * @param idVenda
     * @return objeto Venda ou null
     */
    public Venda buscarPorId(int idVenda) {
        Venda venda = null;
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "SELECT v.id_venda, v.id_cliente, c.nome AS nome_cliente, v.data, " +
                         "v.valor_total, v.status, v.observacao " +
                         "FROM venda v " +
                         "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                         "WHERE v.id_venda = ?";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setInt(1, idVenda);
            bd.rs = bd.st.executeQuery();
            
            if (bd.rs.next()) {
                int idCliente = bd.rs.getInt("id_cliente");
                String nomeCliente = bd.rs.getString("nome_cliente");
                Timestamp ts = bd.rs.getTimestamp("data");
                double valorTotal = bd.rs.getDouble("valor_total");
                String status = bd.rs.getString("status");
                String observacao = bd.rs.getString("observacao");
                
                venda = new Venda(idVenda, idCliente, nomeCliente, 
                                 ts != null ? ts.toLocalDateTime() : null, 
                                 valorTotal, status, observacao);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar venda por ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        return venda;
    }
    
    /**
     * Busca os itens de uma venda específica
     * @param idVenda
     * @return lista de itens
     */
    public List<ItemVenda> buscarItensPorVenda(int idVenda) {
        List<ItemVenda> lista = new ArrayList<>();
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "SELECT iv.id_venda, iv.id_produto, p.nome AS nome_produto, " +
                         "iv.quantidade, iv.preco_unitario, iv.subtotal " +
                         "FROM item_venda iv " +
                         "JOIN produto p ON iv.id_produto = p.id_produto " +
                         "WHERE iv.id_venda = ?";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setInt(1, idVenda);
            bd.rs = bd.st.executeQuery();
            
            while (bd.rs.next()) {
                int idProduto = bd.rs.getInt("id_produto");
                String nomeProduto = bd.rs.getString("nome_produto");
                int quantidade = bd.rs.getInt("quantidade");
                double precoUnitario = bd.rs.getDouble("preco_unitario");
                double subtotal = bd.rs.getDouble("subtotal");
                
                ItemVenda item = new ItemVenda(idVenda, idProduto, nomeProduto, 
                                               quantidade, precoUnitario, subtotal);
                lista.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar itens da venda: " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        return lista;
    }
    
    /**
     * Busca vendas por nome de cliente
     * @param nomeCliente
     * @return lista de vendas
     */
    public List<Venda> buscarPorCliente(String nomeCliente) {
        List<Venda> lista = new ArrayList<>();
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "SELECT v.id_venda, v.id_cliente, c.nome AS nome_cliente, v.data, " +
                         "v.valor_total, v.status, v.observacao " +
                         "FROM venda v " +
                         "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                         "WHERE LOWER(c.nome) LIKE LOWER(?) " +
                         "ORDER BY v.data DESC";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setString(1, "%" + nomeCliente + "%");
            bd.rs = bd.st.executeQuery();
            
            while (bd.rs.next()) {
                int idVenda = bd.rs.getInt("id_venda");
                int idCliente = bd.rs.getInt("id_cliente");
                String nome = bd.rs.getString("nome_cliente");
                Timestamp ts = bd.rs.getTimestamp("data");
                double valorTotal = bd.rs.getDouble("valor_total");
                String status = bd.rs.getString("status");
                String observacao = bd.rs.getString("observacao");
                
                Venda v = new Venda(idVenda, idCliente, nome, 
                                   ts != null ? ts.toLocalDateTime() : null, 
                                   valorTotal, status, observacao);
                lista.add(v);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar vendas por cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        return lista;
    }
    
    /**
     * Busca vendas por status
     * @param status
     * @return lista de vendas
     */
    public List<Venda> buscarPorStatus(String status) {
        List<Venda> lista = new ArrayList<>();
        BD bd = new BD();
        
        try {
            bd.getConnection();
            
            String sql = "SELECT v.id_venda, v.id_cliente, c.nome AS nome_cliente, v.data, " +
                         "v.valor_total, v.status, v.observacao " +
                         "FROM venda v " +
                         "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                         "WHERE v.status = ? " +
                         "ORDER BY v.data DESC";
            
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setString(1, status);
            bd.rs = bd.st.executeQuery();
            
            while (bd.rs.next()) {
                int idVenda = bd.rs.getInt("id_venda");
                int idCliente = bd.rs.getInt("id_cliente");
                String nomeCliente = bd.rs.getString("nome_cliente");
                Timestamp ts = bd.rs.getTimestamp("data");
                double valorTotal = bd.rs.getDouble("valor_total");
                String statusVenda = bd.rs.getString("status");
                String observacao = bd.rs.getString("observacao");
                
                Venda v = new Venda(idVenda, idCliente, nomeCliente, 
                                   ts != null ? ts.toLocalDateTime() : null, 
                                   valorTotal, statusVenda, observacao);
                lista.add(v);
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar vendas por status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }
        
        return lista;
    }
}