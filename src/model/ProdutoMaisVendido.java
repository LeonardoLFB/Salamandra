package model;

public class ProdutoMaisVendido {

    private int idProduto;
    private String nomeProduto;
    private int quantidadeVendida;
    private double valorTotal;

    public ProdutoMaisVendido() {
    }

    public ProdutoMaisVendido(
            int idProduto,
            String nomeProduto,
            int quantidadeVendida,
            double valorTotal) {

        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.quantidadeVendida = quantidadeVendida;
        this.valorTotal = valorTotal;
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

    public int getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public void setQuantidadeVendida(int quantidadeVendida) {
        this.quantidadeVendida = quantidadeVendida;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getValorFormatado() {
        return String.format("R$ %.2f", valorTotal);
    }
}