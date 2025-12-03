package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Usuario;

public class UsuarioDAO extends Usuario {

	public List<Usuario> lista = new ArrayList<Usuario>();

	public String inserir(Usuario u) {
		String s = "Usuario inserido com sucesso!";
		BD bd = new BD();
		bd.getConnection();
		String sql = "INSERT INTO usuario (nome, login, senha, email, tipo_acesso) " + "VALUES (?,?,?,?,?)";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setString(1, u.getNome());
			bd.st.setString(2, u.getEmail());
			bd.st.setString(3, u.getLogin());
			bd.st.setString(4, u.getSenha());
			bd.st.setString(5, u.getTipo());

			int n = bd.st.executeUpdate();

			if (n == 0) {
				s = "Usuario nao encontrado.";
			}

		} catch (SQLException e) {
			s = "Falha na inclusao do usuario " + e;
		} finally {
			bd.close();
		}
		return s;
	}

	public String deletar(int id) {
		String s = "Usuário deletado com sucesso!";
		BD bd = new BD();
		bd.getConnection();

		String sql = "DELETE FROM usuario WHERE id_usuario = ? RETURNING id_usuario";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setInt(1, id);

			bd.rs = bd.st.executeQuery();

			if (!bd.rs.next()) {
				s = "Usuário não encontrado ou não foi possível deletar.";
			} else {
				int deletedId = bd.rs.getInt(1);
				System.out.println("Deleted id: " + deletedId);
			}

		} catch (SQLException e) {
			s = "Falha ao deletar o usuário: " + e.getMessage();
			e.printStackTrace();
		} finally {
			bd.close();
		}
		return s;
	}

	public String atualizar(Usuario u) {
		String s = "Usuário atualizado com sucesso!";
		BD bd = new BD();
		bd.getConnection();

		String sql = "UPDATE usuario SET nome = ?, login = ?, senha = ?, email = ?, tipo_acesso = ? "
				+ "WHERE id_usuario = ?";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setString(1, u.getNome());
			bd.st.setString(2, u.getLogin());
			bd.st.setString(3, u.getSenha());
			bd.st.setString(4, u.getEmail());
			bd.st.setString(5, u.getTipo());
			bd.st.setInt(6, u.getId()); // id do usuário

			int n = bd.st.executeUpdate();

			if (n == 0) {
				s = "Usuário não encontrado.";
			}

		} catch (SQLException e) {
			s = "Falha ao atualizar o usuário: " + e;
		} finally {
			bd.close();
		}

		return s;
	}

	public List<Usuario> getAll() {
		List<Usuario> lista = new ArrayList<>();

		BD bd = new BD();
		bd.getConnection();

		String sql = "SELECT * FROM usuario";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.rs = bd.st.executeQuery();

			while (bd.rs.next()) {

				int id = bd.rs.getInt("id_usuario");
				String nome = bd.rs.getString("nome");
				String login = bd.rs.getString("login");
				String senha = bd.rs.getString("senha");
				String email = bd.rs.getString("email");
				String tipo = bd.rs.getString("tipo_acesso");

				Usuario u = new Usuario(id, nome, login, senha, email, tipo);
				lista.add(u);
			}

		} catch (SQLException e) {
			// NÃO retorne null — apenas logue e continue com lista vazia
			System.err.println("Erro em UsuarioDAO.getAll(): " + e.getMessage());
			e.printStackTrace();
		} finally {
			bd.close();
		}

		System.out.println("UsuarioDAO.getAll() retornou " + lista.size() + " registros.");
		return lista;
	}

	public Usuario autenticar(String login, String senha) {
		Usuario usuario = null;
		BD bd = new BD();
		bd.getConnection();

		String sql = "SELECT * FROM usuario WHERE login = ? AND senha = ?";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setString(1, login);
			bd.st.setString(2, senha);
			bd.rs = bd.st.executeQuery();

			if (bd.rs.next()) {
				int id = bd.rs.getInt("id_usuario");
				String nome = bd.rs.getString("nome");
				String loginDb = bd.rs.getString("login");
				String senhaDb = bd.rs.getString("senha");
				String email = bd.rs.getString("email");
				String tipo = bd.rs.getString("tipo_acesso");

				usuario = new Usuario(id, nome, loginDb, senhaDb, email, tipo);
			}

		} catch (SQLException e) {
			System.err.println("Erro ao autenticar usuário: " + e.getMessage());
			e.printStackTrace();
		} finally {
			bd.close();
		}

		return usuario;
	}
	
	public void criarUsuarioPadrao() {
	    BD bd = new BD();
	    bd.getConnection();
	    
	    // Verifica se já existe algum usuário admin
	    String sqlCheck = "SELECT COUNT(*) FROM usuario WHERE login = 'admin'";
	    
	    try {
	        bd.st = bd.con.prepareStatement(sqlCheck);
	        bd.rs = bd.st.executeQuery();
	        
	        if (bd.rs.next() && bd.rs.getInt(1) == 0) {
	            // Não existe admin, vamos criar
	            String sqlInsert = "INSERT INTO usuario (nome, login, senha, email, tipo_acesso) " +
	                             "VALUES ('Administrador', 'admin', 'admin123', 'admin@sistema.com', 'Administrador')";
	            
	            bd.st = bd.con.prepareStatement(sqlInsert);
	            int resultado = bd.st.executeUpdate();
	            
	            if (resultado > 0) {
	                System.out.println("✅ Usuário admin criado com sucesso!");
	                System.out.println("Login: admin");
	                System.out.println("Senha: admin123");
	            }
	        } else {
	            System.out.println("ℹ️ Usuário admin já existe no banco.");
	        }
	        
	    } catch (SQLException e) {
	        System.err.println("Erro ao criar usuário padrão: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        bd.close();
	    }
	}

}
