package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BD {

	public Connection con = null; //realiza a conexão
	public PreparedStatement st = null; //executa instruções em sql
	public ResultSet rs = null;

	private String URL;
	private String LOGIN;
	private String SENHA;

	public BD() {
	    Properties properties = new Properties();

	    try (InputStream input = getClass()
	            .getClassLoader()
	            .getResourceAsStream("config.properties")) {

	        if (input == null) {
	            System.out.println("Arquivo config.properties não encontrado.");
	            return;
	        }

	        properties.load(input);

	        String host = properties.getProperty("db.host");
	        String port = properties.getProperty("db.port");
	        String database = properties.getProperty("db.name");

	        LOGIN = properties.getProperty("db.user");
	        SENHA = properties.getProperty("db.password");

	        URL = "jdbc:postgresql://" + host + ":" + port + "/" + database;

	    } catch (IOException e) {
	        System.out.println("Erro ao carregar config.properties");
	        e.printStackTrace();
	    }
	}

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
