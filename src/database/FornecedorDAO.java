package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Fornecedor;

/**
 * DAO de Fornecedor. Segue o mesmo padrao dos demais DAOs do projeto
 * (ClienteDAO, ProdutoDAO, UsuarioDAO): cada metodo abre sua propria
 * conexao via BD, usa PreparedStatement e fecha no finally.
 */
public class FornecedorDAO {

    public String inserir(Fornecedor f) {
        String s = "Fornecedor inserido com sucesso!";
        BD bd = new BD();
        bd.getConnection();

        String sql = "INSERT INTO fornecedor (nome, cnpj_cpf, contato, email, endereco, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setString(1, f.getNome());
            bd.st.setString(2, f.getCnpjCpf());
            bd.st.setString(3, f.getContato());
            bd.st.setString(4, f.getEmail());
            bd.st.setString(5, f.getEndereco());
            bd.st.setString(6, f.getStatus());

            int n = bd.st.executeUpdate();

            if (n == 0) {
                s = "Erro ao inserir fornecedor.";
            }

        } catch (SQLException e) {
            s = "Falha na inclusão do fornecedor: " + e.getMessage();
            e.printStackTrace();
        } finally {
            bd.close();
        }
        return s;
    }

    public String atualizar(Fornecedor f) {
        String s = "Fornecedor atualizado com sucesso!";
        BD bd = new BD();
        bd.getConnection();

        String sql = "UPDATE fornecedor SET nome = ?, cnpj_cpf = ?, contato = ?, "
                + "email = ?, endereco = ?, status = ? WHERE id_fornecedor = ?";

        try {
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setString(1, f.getNome());
            bd.st.setString(2, f.getCnpjCpf());
            bd.st.setString(3, f.getContato());
            bd.st.setString(4, f.getEmail());
            bd.st.setString(5, f.getEndereco());
            bd.st.setString(6, f.getStatus());
            bd.st.setInt(7, f.getId());

            int n = bd.st.executeUpdate();

            if (n == 0) {
                s = "Fornecedor não encontrado.";
            }

        } catch (SQLException e) {
            s = "Falha ao atualizar o fornecedor: " + e.getMessage();
            e.printStackTrace();
        } finally {
            bd.close();
        }
        return s;
    }

    public String deletar(int id) {
        String s = "Fornecedor deletado com sucesso!";
        BD bd = new BD();
        bd.getConnection();

        String sql = "DELETE FROM fornecedor WHERE id_fornecedor = ? RETURNING id_fornecedor";

        try {
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setInt(1, id);

            bd.rs = bd.st.executeQuery();

            if (!bd.rs.next()) {
                s = "Fornecedor não encontrado ou não foi possível deletar.";
            } else {
                int deletedId = bd.rs.getInt(1);
                System.out.println("Fornecedor deletado - ID: " + deletedId);
            }

        } catch (SQLException e) {
            s = "Falha ao deletar o fornecedor: " + e.getMessage();
            e.printStackTrace();
        } finally {
            bd.close();
        }
        return s;
    }

    public List<Fornecedor> getAll() {
        List<Fornecedor> lista = new ArrayList<>();

        BD bd = new BD();
        bd.getConnection();

        String sql = "SELECT * FROM fornecedor ORDER BY nome";

        try {
            bd.st = bd.con.prepareStatement(sql);
            bd.rs = bd.st.executeQuery();

            while (bd.rs.next()) {
                Fornecedor f = new Fornecedor(
                        bd.rs.getInt("id_fornecedor"),
                        bd.rs.getString("nome"),
                        bd.rs.getString("cnpj_cpf"),
                        bd.rs.getString("contato"),
                        bd.rs.getString("email"),
                        bd.rs.getString("endereco"),
                        bd.rs.getString("status"));
                lista.add(f);
            }

        } catch (SQLException e) {
            System.err.println("Erro em FornecedorDAO.getAll(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }

        System.out.println("FornecedorDAO.getAll() retornou " + lista.size() + " registros.");
        return lista;
    }

    public Fornecedor buscarPorId(int id) {
        Fornecedor fornecedor = null;
        BD bd = new BD();
        bd.getConnection();

        String sql = "SELECT * FROM fornecedor WHERE id_fornecedor = ?";

        try {
            bd.st = bd.con.prepareStatement(sql);
            bd.st.setInt(1, id);
            bd.rs = bd.st.executeQuery();

            if (bd.rs.next()) {
                fornecedor = new Fornecedor(
                        bd.rs.getInt("id_fornecedor"),
                        bd.rs.getString("nome"),
                        bd.rs.getString("cnpj_cpf"),
                        bd.rs.getString("contato"),
                        bd.rs.getString("email"),
                        bd.rs.getString("endereco"),
                        bd.rs.getString("status"));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar fornecedor por ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            bd.close();
        }

        return fornecedor;
    }
}
