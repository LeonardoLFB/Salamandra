package model;

public class Produto {
	
	private int codigo;
	private String nome;
	private int qtdeEstoque;
	private double preco;
	private String lote;
	private String descricao;
	
	public Produto() {
	}

	public Produto(int codigo, String nome, int qtdeEstoque, double preco, String lote, String descricao) {
		this.codigo = codigo;
		this.nome = nome;
		this.qtdeEstoque = qtdeEstoque;
		this.preco = preco;
		this.lote = lote;
		this.descricao = descricao;
	}

	@Override
	public String toString() {
		return "Produto [codigo=" + codigo + ", nome=" + nome + ", qtdeEstoque=" + qtdeEstoque + ", preco=" + preco
				+ "]";
	}

	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getQtdeEstoque() {
		return qtdeEstoque;
	}
	public void setQtdeEstoque(int qtdeEstoque) {
		this.qtdeEstoque = qtdeEstoque;
	}
	public double getPreco() {
		return preco;
	}
	public void setPreco(double preco) {
		this.preco = preco;
	}
	public String getLote() {
		return lote;
	}
	public void setLote(String lote) {
		this.lote = lote;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

}
