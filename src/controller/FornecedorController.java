package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

public class FornecedorController {

    @FXML private TextField tfNome;
    @FXML private TextField tfCnpj;
    @FXML private TextField tfContato;
    @FXML private TextField tfEndereco;
    @FXML private Button btnCadastrar;
    @FXML private TextField tfSearch;
    @FXML private TableView<Fornecedor> tableFornecedores;

    // Dados observáveis
    private final ObservableList<Fornecedor> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configura colunas (assume que as colunas foram declaradas no FXML e estão na ordem)
        // Se preferir, você pode definir fx:id nas TableColumn no FXML e injetá-las aqui.
        if (tableFornecedores.getColumns().size() >= 5) {
            @SuppressWarnings("unchecked")
			TableColumn<Fornecedor, String> colNome = (TableColumn<Fornecedor, String>) tableFornecedores.getColumns().get(0);
            @SuppressWarnings("unchecked")
			TableColumn<Fornecedor, String> colCnpj = (TableColumn<Fornecedor, String>) tableFornecedores.getColumns().get(1);
            @SuppressWarnings("unchecked")
			TableColumn<Fornecedor, String> colContato = (TableColumn<Fornecedor, String>) tableFornecedores.getColumns().get(2);
            @SuppressWarnings("unchecked")
			TableColumn<Fornecedor, String> colEndereco = (TableColumn<Fornecedor, String>) tableFornecedores.getColumns().get(3);
            @SuppressWarnings("unchecked")
			TableColumn<Fornecedor, String> colAcoes = (TableColumn<Fornecedor, String>) tableFornecedores.getColumns().get(4);

            colNome.setCellValueFactory(cell -> cell.getValue().nomeProperty());
            colCnpj.setCellValueFactory(cell -> cell.getValue().cnpjProperty());
            colContato.setCellValueFactory(cell -> cell.getValue().contatoProperty());
            colEndereco.setCellValueFactory(cell -> cell.getValue().enderecoProperty());

            // A coluna 'AÇÕES' ficará vazia por enquanto; você pode customizar com cellFactory depois.
            colAcoes.setCellValueFactory(cell -> new SimpleStringProperty(""));
        }

        // Search: FilteredList -> SortedList -> bind to table
        FilteredList<Fornecedor> filtered = new FilteredList<>(masterData, p -> true);

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal == null ? "" : newVal.toLowerCase().trim();
            filtered.setPredicate(f -> {
                if (lower.isEmpty()) return true;
                if (f.getNome().toLowerCase().contains(lower)) return true;
                if (f.getCnpj().toLowerCase().contains(lower)) return true;
                if (f.getContato().toLowerCase().contains(lower)) return true;
                if (f.getEndereco().toLowerCase().contains(lower)) return true;
                return false;
            });
        });

        SortedList<Fornecedor> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableFornecedores.comparatorProperty());
        tableFornecedores.setItems(sorted);

        // Botão cadastrar
        btnCadastrar.setOnAction(e -> handleCadastrar());

        // Enter no campo de busca: não faz nada especial, mas evita comportamento inesperado
        tfSearch.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case ENTER:
                    event.consume();
                    break;
                default:
                    break;
            }
        });

        // (Opcional) pre-popular com exemplos
        masterData.add(new Fornecedor("Fornecedor Exemplo Ltda", "12.345.678/0001-90", "contato@fornecedor.com", "Rua das Flores, 123, São Paulo - SP"));
        masterData.add(new Fornecedor("Distribuidora Alfa", "98.765.432/0001-10", "(21) 98765-4321", "Av. Brasil, 456, Rio de Janeiro - RJ"));
    }

    private void handleCadastrar() {
        String nome = safeText(tfNome);
        String cnpj = safeText(tfCnpj);
        String contato = safeText(tfContato);
        String endereco = safeText(tfEndereco);

        if (nome.isEmpty()) {
            alertWarning("Nome obrigatório", "Informe o nome/razão social do fornecedor.");
            return;
        }

        // Adiciona ao list e limpa campos
        Fornecedor novo = new Fornecedor(nome, cnpj, contato, endereco);
        masterData.add(novo);
        clearFields();
    }

    private String safeText(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    private void clearFields() {
        if (tfNome != null) tfNome.clear();
        if (tfCnpj != null) tfCnpj.clear();
        if (tfContato != null) tfContato.clear();
        if (tfEndereco != null) tfEndereco.clear();
        if (tfNome != null) tfNome.requestFocus();
    }

    private void alertWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    // Modelo simples interno — você pode mover para outra classe se quiser
    public static class Fornecedor {
        private final SimpleStringProperty nome = new SimpleStringProperty("");
        private final SimpleStringProperty cnpj = new SimpleStringProperty("");
        private final SimpleStringProperty contato = new SimpleStringProperty("");
        private final SimpleStringProperty endereco = new SimpleStringProperty("");

        public Fornecedor() {}
        public Fornecedor(String nome, String cnpj, String contato, String endereco) {
            this.nome.set(nome);
            this.cnpj.set(cnpj);
            this.contato.set(contato);
            this.endereco.set(endereco);
        }

        public String getNome() { return nome.get(); }
        public void setNome(String v) { nome.set(v); }
        public SimpleStringProperty nomeProperty() { return nome; }

        public String getCnpj() { return cnpj.get(); }
        public void setCnpj(String v) { cnpj.set(v); }
        public SimpleStringProperty cnpjProperty() { return cnpj; }

        public String getContato() { return contato.get(); }
        public void setContato(String v) { contato.set(v); }
        public SimpleStringProperty contatoProperty() { return contato; }

        public String getEndereco() { return endereco.get(); }
        public void setEndereco(String v) { endereco.set(v); }
        public SimpleStringProperty enderecoProperty() { return endereco; }
    }
}
