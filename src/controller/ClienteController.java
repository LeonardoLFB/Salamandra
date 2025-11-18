package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

public class ClienteController {

    @FXML private TextField tfNome;
    @FXML private TextField tfDocumento;
    @FXML private TextField tfTelefone;
    @FXML private TextField tfEmail;
    @FXML private Button btnLimpar;
    @FXML private Button btnSalvar;
    @FXML private TextField tfSearch;
    @FXML private TableView<Cliente> tableClientes;

    private final ObservableList<Cliente> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure table columns (assumes order in FXML)
        if (tableClientes != null && tableClientes.getColumns().size() >= 5) {
            @SuppressWarnings("unchecked")
            TableColumn<Cliente, String> colNome = (TableColumn<Cliente, String>) tableClientes.getColumns().get(0);
            @SuppressWarnings("unchecked")
            TableColumn<Cliente, String> colDocumento = (TableColumn<Cliente, String>) tableClientes.getColumns().get(1);
            @SuppressWarnings("unchecked")
            TableColumn<Cliente, String> colContato = (TableColumn<Cliente, String>) tableClientes.getColumns().get(2);
            @SuppressWarnings("unchecked")
            TableColumn<Cliente, String> colEmail = (TableColumn<Cliente, String>) tableClientes.getColumns().get(3);
            @SuppressWarnings("unchecked")
            TableColumn<Cliente, String> colAcoes = (TableColumn<Cliente, String>) tableClientes.getColumns().get(4);

            colNome.setCellValueFactory(cell -> cell.getValue().nomeProperty());
            colDocumento.setCellValueFactory(cell -> cell.getValue().documentoProperty());
            colContato.setCellValueFactory(cell -> cell.getValue().contatoProperty());
            colEmail.setCellValueFactory(cell -> cell.getValue().emailProperty());

            // coluna ações vazia por enquanto (pode ser customizada com cell factory)
            colAcoes.setCellValueFactory(cell -> new SimpleStringProperty(""));
        }

        // Filtered + Sorted for search
        FilteredList<Cliente> filtered = new FilteredList<>(masterData, p -> true);

        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal == null ? "" : newVal.toLowerCase().trim();
                filtered.setPredicate(c -> {
                    if (lower.isEmpty()) return true;
                    if (c.getNome().toLowerCase().contains(lower)) return true;
                    if (c.getDocumento().toLowerCase().contains(lower)) return true;
                    if (c.getContato().toLowerCase().contains(lower)) return true;
                    if (c.getEmail().toLowerCase().contains(lower)) return true;
                    return false;
                });
            });
        }

        SortedList<Cliente> sorted = new SortedList<>(filtered);
        if (tableClientes != null) {
            sorted.comparatorProperty().bind(tableClientes.comparatorProperty());
            tableClientes.setItems(sorted);
        }

        // Buttons
        if (btnSalvar != null) btnSalvar.setOnAction(e -> handleSalvar());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> clearFields());

        if (tfSearch != null) {
            tfSearch.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()) {
                    case ENTER:
                        event.consume();
                        break;
                    default:
                        break;
                }
            });
        }

        // Sample data
        masterData.add(new Cliente("Ana Clara Souza", "123.456.789-00", "(11) 98765-4321", "ana.souza@example.com"));
        masterData.add(new Cliente("Bruno Mendes Comércio LTDA", "98.765.432/0001-10", "(21) 91234-5678", "contato@brunomendes.com"));
        masterData.add(new Cliente("Carlos Eduardo Lima", "111.222.333-44", "(31) 95555-4444", "carlos.lima@example.com"));
        masterData.add(new Cliente("Daniela Ferreira", "555.666.777-88", "(41) 98888-7777", "daniela.f@example.com"));
    }

    private void handleSalvar() {
        String nome = safeText(tfNome);
        String documento = safeText(tfDocumento);
        String telefone = safeText(tfTelefone);
        String email = safeText(tfEmail);

        if (nome.isEmpty()) {
            alertWarning("Nome obrigatório", "Informe o nome completo ou razão social do cliente.");
            return;
        }

        Cliente novo = new Cliente(nome, documento, telefone, email);
        masterData.add(novo);
        clearFields();
    }

    private String safeText(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    private void clearFields() {
        if (tfNome != null) tfNome.clear();
        if (tfDocumento != null) tfDocumento.clear();
        if (tfTelefone != null) tfTelefone.clear();
        if (tfEmail != null) tfEmail.clear();
        if (tfNome != null) tfNome.requestFocus();
    }

    private void alertWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    // Modelo interno simples
    public static class Cliente {
        private final SimpleStringProperty nome = new SimpleStringProperty("");
        private final SimpleStringProperty documento = new SimpleStringProperty("");
        private final SimpleStringProperty contato = new SimpleStringProperty("");
        private final SimpleStringProperty email = new SimpleStringProperty("");

        public Cliente() {}

        public Cliente(String nome, String documento, String contato, String email) {
            this.nome.set(nome);
            this.documento.set(documento);
            this.contato.set(contato);
            this.email.set(email);
        }

        public String getNome() { return nome.get(); }
        public void setNome(String v) { nome.set(v); }
        public SimpleStringProperty nomeProperty() { return nome; }

        public String getDocumento() { return documento.get(); }
        public void setDocumento(String v) { documento.set(v); }
        public SimpleStringProperty documentoProperty() { return documento; }

        public String getContato() { return contato.get(); }
        public void setContato(String v) { contato.set(v); }
        public SimpleStringProperty contatoProperty() { return contato; }

        public String getEmail() { return email.get(); }
        public void setEmail(String v) { email.set(v); }
        public SimpleStringProperty emailProperty() { return email; }
    }
}
