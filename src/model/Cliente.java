package model;

/**
 * Modelo que representa um cliente no sistema.
 * Compatível com ClienteController e com a tabela do FXML.
 */
public class Cliente {

    private String id;
    private String nome;
    private String cpfCnpj;
    private String contato;
    private String email;
    private String status;

    /** Construtor vazio (requerido para FXML e frameworks). */
    public Cliente() {}

    /**
     * Construtor completo.
     *
     * @param id ID único do cliente (ex: #C1001)
     * @param nome Nome do cliente
     * @param cpfCnpj CPF ou CNPJ
     * @param contato Telefone
     * @param email E-mail
     * @param status Status ("Ativo" ou "Inativo")
     */
    public Cliente(String id, String nome, String cpfCnpj, String contato, String email, String status) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.contato = contato;
        this.email = email;
        this.status = status;
    }

    // ---------------------------------------------------------
    // GETTERS E SETTERS
    // ---------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ---------------------------------------------------------

    @Override
    public String toString() {
        return nome + " (" + cpfCnpj + ")";
    }
}
