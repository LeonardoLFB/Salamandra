package database;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Produto;

//implements DAO (poderia ter uma interface DAO generica pra implementar as partes do codigo
public class ProdutoDAO extends Produto {

	public List<Produto> lista = new ArrayList<Produto>();


	/**
	 * Insere no banco de dados o estado atual do produto
	 * Antes de realizar a inclusão o produto deve estar preenchido
	 * @return - uma mensagem contendo o resultado da operação
	 */
	public String inserir() {
		String s = "Produto inserido com sucesso!";
		BD bd = new BD();
		bd.getConnection();
		String sql = "INSERT INTO produto (nome, codigo, lote, descricao, preco_custo, quantidade_estoque) "
				+ "VALUES (?,?,?,?,?,?)";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setString(1,  getNome());
			bd.st.setInt(2,  getCodigo());
			bd.st.setString(3,  getLote());
			bd.st.setString(4,  getDescricao());
			bd.st.setDouble(5,  getPreco());
			bd.st.setInt(6,  getQtdeEstoque());
			

			int n = bd.st.executeUpdate();
			
			if(n == 0) {
				s = "Produto nao encontrado.";
			}

		}
		catch(SQLException e) {
			s = "Falha na inclusao do produto " + e;
		}
		finally {
			bd.close();
		}
		return s;
	}

	/**
	 * Altera no banco de dados o estado atual do produto
	 * Antes de realizar a alteração o produto deve estar preenchido
	 * @return - uma mensagem contendo o resultado da operação
	 DESATIVEI ATE ARRUMAR - JOAO
	public String alterar() {
		String s = "Produto alterado com sucesso!";
		BD bd = new BD();
		bd.getConnection();
		String sql = "UPDATE produto SET nome=?,qtdeEstoque=?,preco=? WHERE codigo=?";
		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setInt(4,  getCodigo());
			bd.st.setString(1,  getNome());
			bd.st.setInt(2,  getQtdeEstoque());
			bd.st.setDouble(3,  getPreco());
			int linhasAfetadas = bd.st.executeUpdate();

			if(linhasAfetadas == 0) {
				s = "Produto não encontrado.";
			}

		}
		catch(SQLException e) {
			s = "Falha na alteração do produto " + e;
		}
		finally {
			bd.close();
		}
		return s;
	}
	 */
	
	
	/**
	 * Altera no banco de dados o estado atual do produto
	 * Antes de realizar a alteração o produto deve estar preenchido
	 * @return - uma mensagem contendo o resultado da operação
	DESATIVEI ATE ARRUMAR - JOAO
	public String excluir() {
		String s = "Produto excluido com sucesso!";
		BD bd = new BD();
		bd.getConnection();
		String sql = "DELETE FROM produto WHERE codigo=?";
		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setInt(1,  getCodigo());

			int produtosAfetados = bd.st.executeUpdate();			
			if(produtosAfetados == 0) {
				s = "Produto não encontrado.";
			}

		}
		catch(SQLException e) {
			s = "Falha na exclusão do produto " + e;
		}
		finally {
			bd.close();
		}
		return s;
	}
	*/
	/**
	 * Retorna uma lista contendo todos os produtos da tabela
	 * @return
	 */

	public List<Produto> getAll(){
		//nao precisa ser feito a entrada do banco de dados toda hora, da pra fazer direto no construtor sla estudar
		BD bd = new BD();
		bd.getConnection(); 
		String sql = "SELECT * FROM produto";
		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.rs = bd.st.executeQuery();
			while(bd.rs.next()) {
				Produto p = new Produto(bd.rs.getInt(1),bd.rs.getString(2),
						bd.rs.getInt(3),bd.rs.getDouble(4),bd.rs.getString(5),bd.rs.getString(6));
				lista.add(p);
			}
			return lista;
		}
		catch(SQLException e) {
			return null;
		}
		finally {
			bd.close();
		}

	}
	
	/*
	 * Exportar pra excel
	 */
	public String toCSV(){
		String s = "Arquivo CSV gerado com sucesso!";
		BD bd = new BD();
		bd.getConnection(); 
		String sql = "SELECT * FROM produto";
		try {
			PrintWriter pw = new PrintWriter("produtos.csv");
			bd.st = bd.con.prepareStatement(sql);
			bd.rs = bd.st.executeQuery();
			while(bd.rs.next()) {
				pw.print(bd.rs.getInt(1)+";");
				pw.print(bd.rs.getString(2)+";");
				pw.print(bd.rs.getInt(3)+";");
				pw.print(bd.rs.getDouble(4)+"\n");
				pw.print(bd.rs.getString(5)+"\n");
				pw.print(bd.rs.getString(6)+"\n");
			}
			pw.close();
			
			
		}
		catch(Exception e) {
			s = "Falha ao gerar arquivo CSV.";
		}
		finally {
			bd.close();
		}
		return s;

	}

}
