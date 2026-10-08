package database;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import application.SessaoUsuario;
import model.Auditoria;
import model.Cliente;
import model.Usuario;

public class ClienteDAO {

    // ============================================================
    // INSERIR CLIENTE
    // ============================================================

    public String inserir(Cliente c) {

        String s = "Cliente inserido com sucesso!";

        BD bd = new BD();
        bd.getConnection();

        String sql =
                "INSERT INTO cliente " +
                "(nome, email, cpf, rua, numero, bairro, cidade, estado, cep) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING id_cliente";

        try {

            bd.st = bd.con.prepareStatement(sql);

            bd.st.setString(1, c.getNome());
            bd.st.setString(2, c.getEmail());
            bd.st.setString(3, c.getCpf());
            bd.st.setString(4, c.getRua());
            bd.st.setString(5, c.getNumero());
            bd.st.setString(6, c.getBairro());
            bd.st.setString(7, c.getCidade());
            bd.st.setString(8, c.getEstado());
            bd.st.setString(9, c.getCep());

            bd.rs = bd.st.executeQuery();

            if (bd.rs.next()) {

                int idClienteGerado =
                        bd.rs.getInt("id_cliente");

                c.setId(idClienteGerado);

                System.out.println(
                        "Cliente inserido - ID: "
                        + idClienteGerado
                );

                // ========================================================
                // AUDITORIA - CADASTRO
                // ========================================================

                Usuario usuarioLogado =
                        SessaoUsuario.getUsuarioLogado();

                if (usuarioLogado != null) {

                    Auditoria auditoria =
                            new Auditoria(
                                    usuarioLogado.getId(),
                                    "CADASTRO_CLIENTE",
                                    "Cadastrou o cliente #"
                                    + idClienteGerado
                                    + " - "
                                    + c.getNome()
                            );

                    new AuditoriaDAO()
                            .registrar(auditoria);
                }

            } else {

                s = "Erro ao inserir cliente.";
            }

        } catch (SQLException e) {

            s =
                    "Falha na inclusão do cliente: "
                    + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }

        return s;
    }


    // ============================================================
    // ATUALIZAR CLIENTE
    // ============================================================

    public String atualizar(Cliente c) {

        String s =
                "Cliente atualizado com sucesso!";

        BD bd = new BD();
        bd.getConnection();

        String sql =
                "UPDATE cliente SET " +
                "nome = ?, " +
                "email = ?, " +
                "cpf = ?, " +
                "rua = ?, " +
                "numero = ?, " +
                "bairro = ?, " +
                "cidade = ?, " +
                "estado = ?, " +
                "cep = ? " +
                "WHERE id_cliente = ?";

        try {

            bd.st =
                    bd.con.prepareStatement(sql);

            bd.st.setString(1, c.getNome());
            bd.st.setString(2, c.getEmail());
            bd.st.setString(3, c.getCpf());
            bd.st.setString(4, c.getRua());
            bd.st.setString(5, c.getNumero());
            bd.st.setString(6, c.getBairro());
            bd.st.setString(7, c.getCidade());
            bd.st.setString(8, c.getEstado());
            bd.st.setString(9, c.getCep());
            bd.st.setInt(10, c.getId());

            int n =
                    bd.st.executeUpdate();

            if (n == 0) {

                s =
                        "Cliente não encontrado.";

            } else {

                System.out.println(
                        "Cliente atualizado - ID: "
                        + c.getId()
                );

                // ========================================================
                // AUDITORIA - ALTERAÇÃO
                // ========================================================

                Usuario usuarioLogado =
                        SessaoUsuario.getUsuarioLogado();

                if (usuarioLogado != null) {

                    Auditoria auditoria =
                            new Auditoria(
                                    usuarioLogado.getId(),
                                    "ALTERACAO_CLIENTE",
                                    "Alterou o cliente #"
                                    + c.getId()
                                    + " - "
                                    + c.getNome()
                            );

                    new AuditoriaDAO()
                            .registrar(auditoria);
                }
            }

        } catch (SQLException e) {

            s =
                    "Falha ao atualizar o cliente: "
                    + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }

        return s;
    }


    // ============================================================
    // DELETAR CLIENTE
    // ============================================================

    public String deletar(int id) {

        String s =
                "Cliente deletado com sucesso!";

        BD bd = new BD();
        bd.getConnection();

        String sql =
                "DELETE FROM cliente " +
                "WHERE id_cliente = ? " +
                "RETURNING id_cliente, nome";

        try {

            bd.st =
                    bd.con.prepareStatement(sql);

            bd.st.setInt(1, id);

            bd.rs =
                    bd.st.executeQuery();

            if (!bd.rs.next()) {

                s =
                        "Cliente não encontrado ou não foi possível deletar.";

            } else {

                int deletedId =
                        bd.rs.getInt(
                                "id_cliente"
                        );

                String nomeCliente =
                        bd.rs.getString(
                                "nome"
                        );

                System.out.println(
                        "Cliente deletado - ID: "
                        + deletedId
                );

                // ========================================================
                // AUDITORIA - EXCLUSÃO
                // ========================================================

                Usuario usuarioLogado =
                        SessaoUsuario.getUsuarioLogado();

                if (usuarioLogado != null) {

                    Auditoria auditoria =
                            new Auditoria(
                                    usuarioLogado.getId(),
                                    "EXCLUSAO_CLIENTE",
                                    "Excluiu o cliente #"
                                    + deletedId
                                    + " - "
                                    + nomeCliente
                            );

                    new AuditoriaDAO()
                            .registrar(auditoria);
                }
            }

        } catch (SQLException e) {

            s =
                    "Falha ao deletar o cliente: "
                    + e.getMessage();

            e.printStackTrace();

        } finally {

            bd.close();
        }

        return s;
    }


    // ============================================================
    // LISTAR CLIENTES
    // ============================================================

    public List<Cliente> getAll() {

        List<Cliente> lista =
                new ArrayList<>();

        BD bd =
                new BD();

        bd.getConnection();

        String sql =
                "SELECT * FROM cliente ORDER BY nome";

        try {

            bd.st =
                    bd.con.prepareStatement(sql);

            bd.rs =
                    bd.st.executeQuery();

            while (bd.rs.next()) {

                int id =
                        bd.rs.getInt("id_cliente");

                String nome =
                        bd.rs.getString("nome");

                String email =
                        bd.rs.getString("email");

                String cpf =
                        bd.rs.getString("cpf");

                String rua =
                        bd.rs.getString("rua");

                String numero =
                        bd.rs.getString("numero");

                String bairro =
                        bd.rs.getString("bairro");

                String cidade =
                        bd.rs.getString("cidade");

                String estado =
                        bd.rs.getString("estado");

                String cep =
                        bd.rs.getString("cep");

                Timestamp ts =
                        bd.rs.getTimestamp(
                                "data_cadastro"
                        );

                Cliente c =
                        new Cliente(
                                id,
                                nome,
                                email,
                                cpf,
                                rua,
                                numero,
                                bairro,
                                cidade,
                                estado,
                                cep,
                                ts != null
                                        ? ts.toLocalDateTime()
                                        : null
                        );

                lista.add(c);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro em ClienteDAO.getAll(): "
                    + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            bd.close();
        }

        System.out.println(
                "ClienteDAO.getAll() retornou "
                + lista.size()
                + " registros."
        );

        return lista;
    }


    // ============================================================
    // BUSCAR CLIENTE POR ID
    // ============================================================

    public Cliente buscarPorId(int id) {

        Cliente cliente = null;

        BD bd = new BD();
        bd.getConnection();

        String sql =
                "SELECT * FROM cliente " +
                "WHERE id_cliente = ?";

        try {

            bd.st =
                    bd.con.prepareStatement(sql);

            bd.st.setInt(1, id);

            bd.rs =
                    bd.st.executeQuery();

            if (bd.rs.next()) {

                String nome =
                        bd.rs.getString("nome");

                String email =
                        bd.rs.getString("email");

                String cpf =
                        bd.rs.getString("cpf");

                String rua =
                        bd.rs.getString("rua");

                String numero =
                        bd.rs.getString("numero");

                String bairro =
                        bd.rs.getString("bairro");

                String cidade =
                        bd.rs.getString("cidade");

                String estado =
                        bd.rs.getString("estado");

                String cep =
                        bd.rs.getString("cep");

                Timestamp ts =
                        bd.rs.getTimestamp(
                                "data_cadastro"
                        );

                cliente =
                        new Cliente(
                                id,
                                nome,
                                email,
                                cpf,
                                rua,
                                numero,
                                bairro,
                                cidade,
                                estado,
                                cep,
                                ts != null
                                        ? ts.toLocalDateTime()
                                        : null
                        );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar cliente por ID: "
                    + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            bd.close();
        }

        return cliente;
    }
}