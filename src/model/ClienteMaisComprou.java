package model;

public class ClienteMaisComprou {

    private int idCliente;
    private String nomeCliente;
    private int quantidadeCompras;
    private double valorTotal;

    public ClienteMaisComprou() {
    }

    public ClienteMaisComprou(
            int idCliente,
            String nomeCliente,
            int quantidadeCompras,
            double valorTotal) {

        this.idCliente = idCliente;
        this.nomeCliente = nomeCliente;
        this.quantidadeCompras = quantidadeCompras;
        this.valorTotal = valorTotal;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public int getQuantidadeCompras() {
        return quantidadeCompras;
    }

    public void setQuantidadeCompras(int quantidadeCompras) {
        this.quantidadeCompras = quantidadeCompras;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getValorFormatado() {
        return String.format(
                "R$ %.2f",
                valorTotal
        );
    }
}