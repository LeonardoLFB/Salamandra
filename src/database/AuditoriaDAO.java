package database;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Auditoria;

public class AuditoriaDAO {

    public boolean registrar(Auditoria auditoria) {

        BD bd = new BD();

        try {

            bd.getConnection();

            String sql =
                    "INSERT INTO auditoria " +
                    "(id_usuario, acao, descricao) " +
                    "VALUES (?, ?, ?)";

            bd.st = bd.con.prepareStatement(sql);

            bd.st.setInt(
                    1,
                    auditoria.getIdUsuario()
            );

            bd.st.setString(
                    2,
                    auditoria.getAcao()
            );

            bd.st.setString(
                    3,
                    auditoria.getDescricao()
            );

            bd.st.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao registrar auditoria: "
                    + e.getMessage()
            );

            e.printStackTrace();

            return false;

        } finally {

            bd.close();
        }
    }

    public List<Auditoria> getAll() {

        List<Auditoria> lista =
                new ArrayList<>();

        BD bd = new BD();

        try {

            bd.getConnection();

            String sql =
                    "SELECT " +
                    "a.id_auditoria, " +
                    "a.id_usuario, " +
                    "u.nome AS nome_usuario, " +
                    "a.acao, " +
                    "a.descricao, " +
                    "a.data_hora " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuario u " +
                    "ON a.id_usuario = u.id_usuario " +
                    "ORDER BY a.data_hora DESC";

            bd.st = bd.con.prepareStatement(sql);
            bd.rs = bd.st.executeQuery();

            while (bd.rs.next()) {

                Auditoria auditoria =
                        new Auditoria();

                auditoria.setIdAuditoria(
                        bd.rs.getInt("id_auditoria")
                );

                auditoria.setIdUsuario(
                        bd.rs.getInt("id_usuario")
                );

                auditoria.setNomeUsuario(
                        bd.rs.getString("nome_usuario")
                );

                auditoria.setAcao(
                        bd.rs.getString("acao")
                );

                auditoria.setDescricao(
                        bd.rs.getString("descricao")
                );

                Timestamp timestamp =
                        bd.rs.getTimestamp("data_hora");

                if (timestamp != null) {

                    auditoria.setDataHora(
                            timestamp.toLocalDateTime()
                    );
                }

                lista.add(auditoria);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao buscar auditoria: "
                    + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            bd.close();
        }

        return lista;
    }
}