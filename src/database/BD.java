package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BD {

	public Connection con = null; //realiza a conexão
	public PreparedStatement st = null; //executa instruções em sql
	public ResultSet rs = null;

	public final String DATABASE = "salamandra_incensaria";
	public final String DRIVER = "org.postgresql.Driver";
	public final String URL = "jdbc:postgresql://localhost:5432/"+DATABASE;
	public final String LOGIN = "postgres";
	public final String SENHA = "1234";

	/**
	 * Realiza a conexão ao banco de dados
	 * @return - true em caso de sucesso, ou false caos contrário
	 */
	public boolean getConnection(){
		try {
			con = DriverManager.getConnection(URL,LOGIN,SENHA);
			System.out.println("Conectou BD");
			return true;
		}
		catch(SQLException erro) {
			System.out.println("Falha na conexão " + erro);
			return false;
		}
	}

	public void close() {
		try {
			if(rs!=null) rs.close();
		}
		catch(SQLException e) {}
		try {
			if(st!=null) st.close();
		}
		catch(SQLException e) {}
		try {
			if(con!=null) {
				con.close();
				System.out.println("Desconectou...");
			}
		}
		catch(SQLException e) {
			
		}
	}
	
	public static void main(String[] args) {
		BD bd = new BD();
		bd.getConnection();
		//realizo a ação
	}

}
