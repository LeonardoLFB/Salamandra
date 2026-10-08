package model;

import java.text.NumberFormat;
import java.util.Locale;

public class RelatorioLucroProduto {

    private int idProduto;
    private String nomeProduto;
    private int quantidadeVendida;
    private double faturamento;
    private double custo;
    private double lucro;
    private double margem;

    public RelatorioLucroProduto() {
    }

    public RelatorioLucroProduto(
            int idProduto,
            String nomeProduto,
            int quantidadeVendida,
            double faturamento,
            double custo,
            double lucro,
            double margem) {

        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.quantidadeVendida = quantidadeVendida;
        this.faturamento = faturamento;
        this.custo = custo;
        this.lucro = lucro;
        this.margem = margem;
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

    public double getFaturamento() {
        return faturamento;
    }

    public void setFaturamento(double faturamento) {
        this.faturamento = faturamento;
    }

    public double getCusto() {
        return custo;
    }

    public void setCusto(double custo) {
        this.custo = custo;
    }

    public double getLucro() {
        return lucro;
    }

    public void setLucro(double lucro) {
        this.lucro = lucro;
    }

    public double getMargem() {
        return margem;
    }

    public void setMargem(double margem) {
        this.margem = margem;
    }

    public String getFaturamentoFormatado() {
        return formatarMoeda(faturamento);
    }

    public String getCustoFormatado() {
        return formatarMoeda(custo);
    }

    public String getLucroFormatado() {
        return formatarMoeda(lucro);
    }

    public String getMargemFormatada() {
        return String.format(
                new Locale("pt", "BR"),
                "%.2f%%",
                margem
        );
    }

    private String formatarMoeda(double valor) {

        NumberFormat moeda =
                NumberFormat.getCurrencyInstance(
                        new Locale("pt", "BR")
                );

        return moeda.format(valor);
    }
}