package model;

import java.time.LocalDateTime;

public class Auditoria {

    private int idAuditoria;
    private int idUsuario;
    private String nomeUsuario;
    private String acao;
    private String descricao;
    private LocalDateTime dataHora;

    public Auditoria() {
    }

    public Auditoria(
            int idUsuario,
            String acao,
            String descricao) {

        this.idUsuario = idUsuario;
        this.acao = acao;
        this.descricao = descricao;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}