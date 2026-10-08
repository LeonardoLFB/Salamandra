package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoteProduto {

    private int id;
    private int idProduto;
    private String codigoLote;
    private int quantidade;
    private BigDecimal custoUnitario;
    private LocalDateTime dataEntrada;


    public LoteProduto() {
    }


    public LoteProduto(
            int id,
            int idProduto,
            String codigoLote,
            int quantidade,
            BigDecimal custoUnitario,
            LocalDateTime dataEntrada) {

        this.id = id;
        this.idProduto = idProduto;
        this.codigoLote = codigoLote;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.dataEntrada = dataEntrada;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public int getIdProduto() {
        return idProduto;
    }


    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }


    public String getCodigoLote() {
        return codigoLote;
    }


    public void setCodigoLote(String codigoLote) {
        this.codigoLote = codigoLote;
    }


    public int getQuantidade() {
        return quantidade;
    }


    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }


    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }


    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }


    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }


    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }
}