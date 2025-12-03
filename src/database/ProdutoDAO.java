package database;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Produto;

//implements DAO (poderia ter uma interface DAO generica pra implementar as partes do codigo
@SuppressWarnings("unused")
public class ProdutoDAO extends Produto {

	public List<Produto> lista = new ArrayList<Produto>();


	/**
	 * Insere no banco de dados o estado atual do produto
	 * Antes de realizar a inclusão o produto deve estar preenchido
	 * @param p 
	 * @return - uma mensagem contendo o resultado da operação
	 */
	public String inserir(Produto p) {
		String s = "Produto inserido com sucesso!";
		BD bd = new BD();
		bd.getConnection();
		String sql = "INSERT INTO produto (nome, codigo, lote, descricao, preco_custo, preco_venda, quantidade_estoque) "
				+ "VALUES (?,?,?,?,?,?,?)";

		try {
			bd.st = bd.con.prepareStatement(sql);
			bd.st.setString(1, p.getNome());
			bd.st.setInt(2,  p.getCodigo());
			bd.st.setString(3,  p.getLote());
			bd.st.setString(4,  p.getDescricao());
			bd.st.setDouble(5,  p.getPrecoCusto());
			bd.st.setDouble(6,  p.getPrecoVenda());
			bd.st.setInt(7, p.getQtdeEstoque());
			

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
	 * 
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
	
	 
	
	
	/**
	 * Altera no banco de dados o estado atual do produto
	 * Antes de realizar a alteração o produto deve estar preenchido
	 * @return - uma mensagem contendo o resultado da operação
	 * */
	public String deletar(int id) {
    String s = "Produto deletado com sucesso!";
    BD bd = new BD();
    bd.getConnection();

    String sql = "DELETE FROM produto WHERE id_produto = ? RETURNING id_produto";

    try {
        bd.st = bd.con.prepareStatement(sql);
        bd.st.setInt(1, id);

        bd.rs = bd.st.executeQuery();

        if (!bd.rs.next()) {
            s = "Produto não encontrado ou não foi possível deletar.";
        } else {
            int deletedId = bd.rs.getInt(1);
            System.out.println("Produto deletado - ID: " + deletedId);
        }

    } catch (SQLException e) {
        s = "Falha ao deletar o produto: " + e.getMessage();
        e.printStackTrace();
    } finally {
        bd.close();
    }
    return s;
}

	public List<Produto> getAll() {
	    List<Produto> lista = new ArrayList<>();
	    
	    BD bd = new BD();
	    bd.getConnection();
	    
	    String sql = "SELECT * FROM produto";
	    
	    try {
	        bd.st = bd.con.prepareStatement(sql);
	        bd.rs = bd.st.executeQuery();
	        
	        while (bd.rs.next()) {
	            int id = bd.rs.getInt("id_produto");
	            int codigo = bd.rs.getInt("codigo");
	            String nome = bd.rs.getString("nome");
	            int qtdeEstoque = bd.rs.getInt("quantidade_estoque");
	            double preco_custo = bd.rs.getDouble("preco_custo");
	            double preco_venda = bd.rs.getDouble("preco_venda");
	            String lote = bd.rs.getString("lote");
	            String descricao = bd.rs.getString("descricao");
	            
	            Produto p = new Produto(id, codigo, nome, qtdeEstoque, preco_custo, preco_venda, lote, descricao);
	            lista.add(p);
	        }
	        
	    } catch (SQLException e) {
	        System.err.println("Erro em ProdutoDAO.getAll(): " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        bd.close();
	    }
	    
	    System.out.println("ProdutoDAO.getAll() retornou " + lista.size() + " registros.");
	    return lista;
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
				pw.print(bd.rs.getString(4)+"\n");
				pw.print(bd.rs.getString(5)+"\n");
				pw.print(bd.rs.getDouble(6)+"\n");
				pw.print(bd.rs.getDouble(7)+"\n");
				pw.print(bd.rs.getInt(8)+";");
				pw.print(bd.rs.getInt(9)+";");
				pw.print(bd.rs.getBoolean(10)+";");

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
