package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Venda {

    private String id;
    private String cliente;
    private LocalDate data;
    private double valor;
    private String status;

    private String produtos;
    private int quantidade;
    private String formaPagamento;

    public Venda(int idNumerico, String cliente, LocalDate data, double valor, String status) {
        this.id = String.format("#%05d", idNumerico);
        this.cliente = cliente;
        this.data = data;
        this.valor = valor;
        this.status = status;
    }

    public String getId() { return id; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProdutos() { return produtos; }
    public void setProdutos(String produtos) { this.produtos = produtos; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getDataStr() {
        if (data == null) return "";
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
