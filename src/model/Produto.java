package model;

public class Produto {
	
	private int id;
	private int codigo;
	private String nome;
	private int qtdeEstoque;
	private double preco_custo;
	private double preco_venda;
	private String lote;
	private String descricao;
	
	public Produto() {
	}

	public Produto(int codigo, String nome, int qtdeEstoque, double preco_custo, double preco_venda, String lote, String descricao) {
		this.codigo = codigo;
		this.nome = nome;
		this.qtdeEstoque = qtdeEstoque;
		this.preco_custo = preco_custo;
		this.preco_venda = preco_venda;
		this.lote = lote;
		this.descricao = descricao;
	}
	public Produto(int id, int codigo, String nome, int qtdeEstoque, double preco_custo, double preco_venda, String lote, String descricao) {
		this.id = id;
		this.codigo = codigo;
		this.nome = nome;
		this.qtdeEstoque = qtdeEstoque;
		this.preco_custo = preco_custo;
		this.preco_venda = preco_venda;
		this.lote = lote;
		this.descricao = descricao;
	}

	@Override
	public String toString() {
		return "Produto [codigo=" + codigo + ", nome=" + nome + ", qtdeEstoque=" + qtdeEstoque + ", preco_custo=" + preco_custo + ", + preco_venda" + preco_venda
				+ "]";
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public double getPrecoCusto() {
		return preco_custo;
	}
	public void setPrecoCusto(double preco_custo) {
		this.preco_custo = preco_custo;
	}
	public double getPrecoVenda() {
		return preco_venda;
	}
	public void setPrecoVenda(double preco_venda) {
		this.preco_venda = preco_venda;
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
