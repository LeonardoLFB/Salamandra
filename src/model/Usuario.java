package model;

public class Usuario {

	private int id;
	private String nome;
	private String email;
	private String login;
	private String senha;
	private String tipo;

	public Usuario() {

	}

	public Usuario(String nome, String email, String login, String senha, String tipo) {
		this.nome = nome;
		this.email = email;
		this.login = login;
		this.senha = senha;
		this.tipo = tipo;
	}
	
	public Usuario(int id, String nome, String login, String senha, String email, String tipo) {
	    this.id = id;
	    this.nome = nome;
	    this.login = login;
	    this.senha = senha;
	    this.email = email;
	    this.tipo = tipo;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
