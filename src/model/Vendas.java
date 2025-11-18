package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Versão simples de uma Venda.
 * Mantém uma lista de itens (nome, preço unitário, quantidade),
 * calcula total e permite finalizar/cancelar a venda.
 */
public class Vendas {

    private static int sequenciaId = 1;

    private int id;
    private LocalDateTime dataHora;
    private List<VendaItem> itens;
    private boolean finalizada;
    private boolean cancelada;

    public Vendas() {
        this.id = sequenciaId++;
        this.dataHora = LocalDateTime.now();
        this.itens = new ArrayList<>();
        this.finalizada = false;
        this.cancelada = false;
    }

    /** Item simples da venda */
    public static class VendaItem {
        private String nomeProduto;
        private double precoUnitario;
        private int quantidade;

        public VendaItem(String nomeProduto, double precoUnitario, int quantidade) {
            if (nomeProduto == null || nomeProduto.isEmpty()) throw new IllegalArgumentException("Nome do produto inválido.");
            if (precoUnitario < 0) throw new IllegalArgumentException("Preço inválido.");
            if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
            this.nomeProduto = nomeProduto;
            this.precoUnitario = precoUnitario;
            this.quantidade = quantidade;
        }

        public double getSubtotal() {
            return precoUnitario * quantidade;
        }

        @Override
        public String toString() {
            return nomeProduto + " x" + quantidade + " = " + String.format("%.2f", getSubtotal());
        }
    }

    /** Adiciona um item (se o mesmo nome já existir, soma a quantidade). */
    public void adicionarItem(String nomeProduto, double precoUnitario, int quantidade) {
        if (finalizada || cancelada) throw new IllegalStateException("Venda já encerrada.");
        for (VendaItem item : itens) {
            if (item.nomeProduto.equals(nomeProduto)) {
                item.quantidade += quantidade;
                return;
            }
        }
        itens.add(new VendaItem(nomeProduto, precoUnitario, quantidade));
    }

    /** Remove um item pela nome do produto (retorna true se removeu). */
    public boolean removerItem(String nomeProduto) {
        if (finalizada || cancelada) throw new IllegalStateException("Venda já encerrada.");
        Iterator<VendaItem> it = itens.iterator();
        while (it.hasNext()) {
            if (it.next().nomeProduto.equals(nomeProduto)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /** Calcula o total bruto da venda. */
    public double getTotal() {
        double total = 0.0;
        for (VendaItem item : itens) total += item.getSubtotal();
        return total;
    }

    /** Finaliza a venda. */
    public void finalizar() {
        if (cancelada) throw new IllegalStateException("Venda foi cancelada.");
        this.finalizada = true;
        this.dataHora = LocalDateTime.now();
    }

    /** Cancela a venda (só se não estiver finalizada). */
    public void cancelar() {
        if (finalizada) throw new IllegalStateException("Venda já finalizada.");
        this.cancelada = true;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public List<VendaItem> getItens() {
        return itens;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VendaSimples{id=").append(id)
          .append(", data=").append(dataHora)
          .append(", finalizada=").append(finalizada)
          .append(", cancelada=").append(cancelada)
          .append(", itens=[");
        for (int i = 0; i < itens.size(); i++) {
            sb.append(itens.get(i).toString());
            if (i < itens.size()-1) sb.append(", ");
        }
        sb.append("], total=").append(String.format("%.2f", getTotal())).append("}");
        return sb.toString();
    }
}
