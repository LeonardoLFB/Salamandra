package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Venda {
    private int idVenda;
    private int idCliente;
    private String nomeCliente; // Para exibição na tabela
    private LocalDateTime data;
    private double valorTotal;
    private String status;
    private String observacao;
    
    public Venda() {
    }
    
    public Venda(int idVenda, int idCliente, LocalDateTime data, double valorTotal, String status) {
        this.idVenda = idVenda;
        this.idCliente = idCliente;
        this.data = data;
        this.valorTotal = valorTotal;
        this.status = status;
    }
    
    public Venda(int idVenda, int idCliente, String nomeCliente, LocalDateTime data, double valorTotal, String status, String observacao) {
        this.idVenda = idVenda;
        this.idCliente = idCliente;
        this.nomeCliente = nomeCliente;
        this.data = data;
        this.valorTotal = valorTotal;
        this.status = status;
        this.observacao = observacao;
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
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

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    // Métodos auxiliares para exibição na tabela
    public String getDataFormatada() {
        if (data != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return data.format(formatter);
        }
        return "";
    }
    
    public String getValorFormatado() {
        return String.format("R$ %.2f", valorTotal);
    }
}