package model;

/**
 * Modelo (POJO) que representa um fornecedor.
 * Use este modelo nas TableView e no controller.
 */
public class Fornecedor {

    private Integer id;         // opcional, pode ser null antes de persistir no BD
    private String nome;
    private String cnpjCpf;
    private String contato;
    private String email;
    private String endereco;
    private String status;      // ex.: "Ativo", "Inativo"

    /** Construtor vazio (necessário para frameworks/serialização). */
    public Fornecedor() { }

    /**
     * Construtor sem id — usado ao criar novos fornecedores.
     */
    public Fornecedor(String nome, String cnpjCpf, String contato, String email, String endereco, String status) {
        this.nome = nome;
        this.cnpjCpf = cnpjCpf;
        this.contato = contato;
        this.email = email;
        this.endereco = endereco;
        this.status = status;
    }

    /**
     * Construtor completo com id — usado ao carregar do banco.
     */
    public Fornecedor(Integer id, String nome, String cnpjCpf, String contato, String email, String endereco, String status) {
        this.id = id;
        this.nome = nome;
        this.cnpjCpf = cnpjCpf;
        this.contato = contato;
        this.email = email;
        this.endereco = endereco;
        this.status = status;
    }

    // -------------- Getters e Setters --------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpjCpf() {
        return cnpjCpf;
    }

    public void setCnpjCpf(String cnpjCpf) {
        this.cnpjCpf = cnpjCpf;
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // -------------- Utilitários --------------

    @Override
    public String toString() {
        return nome + (cnpjCpf != null && !cnpjCpf.isBlank() ? " (" + cnpjCpf + ")" : "");
    }
}
