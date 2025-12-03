package model;

public class ItemVenda {
    private int idVenda;
    private int idProduto;
    private String nomeProduto; // Para exibição
    private int quantidade;
    private double precoUnitario;
    private double subtotal;
    
    public ItemVenda() {
    }
    
    public ItemVenda(int idVenda, int idProduto, int quantidade, double precoUnitario, double subtotal) {
        this.idVenda = idVenda;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }
    
    public ItemVenda(int idVenda, int idProduto, String nomeProduto, int quantidade, double precoUnitario, double subtotal) {
        this.idVenda = idVenda;
        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    public void calcularSubtotal() {
        this.subtotal = this.quantidade * this.precoUnitario;
    }
}