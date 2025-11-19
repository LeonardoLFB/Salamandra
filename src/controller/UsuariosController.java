package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.Optional;

public class UsuariosController {

    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<String> cbNivelAcesso;
    @FXML private Button btnCadastrar;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroNivel;
    @FXML private ComboBox<String> cbFiltroStatus;

    @FXML private TableView<User> tableUsuarios;
    @FXML private TableColumn<User, String> colNome;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colNivel;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colAcoes;

    private final ObservableList<User> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Proteção contra FXML não injetado (útil para debugar)
        if (colNome == null) {
            System.err.println("FXML injection failed: check fx:id and controller package/class.");
            return;
        }

        // Configura colunas
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivel"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Formatar coluna status com badge (Label estilizada)
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.setStyle("-fx-padding:4 10; -fx-background-radius:12; -fx-font-size:11;");
                    String s = status.toLowerCase();
                    if (s.contains("ativo")) {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#dcfce7; -fx-text-fill:#065f46;");
                    } else if (s.contains("inativo")) {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#ffe4e6; -fx-text-fill:#991b1b;");
                    } else {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#eef2ff; -fx-text-fill:#3730a3;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Ações (editar / excluir)
        addActionsColumn();

        // Dados de exemplo
        data.addAll(
                new User("João Pereira", "joao.pereira@email.com", "Administrador", "Ativo"),
                new User("Ana Costa", "ana.costa@email.com", "Vendedor", "Ativo"),
                new User("Carlos Souza", "carlos.souza@email.com", "Estoquista", "Inativo")
        );
        tableUsuarios.setItems(data);

        // Preenchendo combos
        cbNivelAcesso.getItems().addAll("Administrador", "Vendedor", "Estoquista");
        cbNivelAcesso.getSelectionModel().selectFirst();

        cbFiltroNivel.getItems().addAll("Todos", "Administrador", "Vendedor", "Estoquista");
        cbFiltroNivel.getSelectionModel().select("Todos");

        cbFiltroStatus.getItems().addAll("Todos", "Ativo", "Inativo");
        cbFiltroStatus.getSelectionModel().select("Todos");

        // Eventos
        btnCadastrar.setOnAction(e -> onCadastrar());
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        cbFiltroNivel.setOnAction(e -> applyFilters());
        cbFiltroStatus.setOnAction(e -> applyFilters());
    }

    /* ================== AÇÕES ================== */

    private void onCadastrar() {
        String nome = txtNome.getText() == null ? "" : txtNome.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String senha = txtSenha.getText() == null ? "" : txtSenha.getText().trim();
        String nivel = cbNivelAcesso.getValue();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Preencha nome, e-mail e senha.").showAndWait();
            return;
        }

        User u = new User(nome, email, nivel, "Ativo");
        data.add(u);
        tableUsuarios.setItems(data);

        // limpa formulário
        txtNome.clear();
        txtEmail.clear();
        txtSenha.clear();
        cbNivelAcesso.getSelectionModel().selectFirst();

        new Alert(Alert.AlertType.INFORMATION, "Usuário cadastrado com sucesso.").showAndWait();
    }

    private void applyFilters() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase();
        String nivel = cbFiltroNivel.getValue();
        String status = cbFiltroStatus.getValue();

        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User u : data) {
            boolean matchesQ = u.getNome().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q);
            boolean matchesNivel = nivel == null || nivel.equals("Todos") || u.getNivel().equals(nivel);
            boolean matchesStatus = status == null || status.equals("Todos") || u.getStatus().equalsIgnoreCase(status);
            if (matchesQ && matchesNivel && matchesStatus) filtered.add(u);
        }
        tableUsuarios.setItems(filtered);
    }

    private void addActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDel = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
                btnDel.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

                btnEdit.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    showEditDialog(u);
                });

                btnDel.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover usuário " + u.getNome() + " ?");
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        data.remove(u);
                        tableUsuarios.setItems(data);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(box);
            }
        };

        colAcoes.setCellFactory(cellFactory);
    }

    private void showEditDialog(User u) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Editar usuário");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nome = new TextField(u.getNome());
        TextField email = new TextField(u.getEmail());
        ComboBox<String> nivel = new ComboBox<>(FXCollections.observableArrayList("Administrador", "Vendedor", "Estoquista"));
        nivel.getSelectionModel().select(u.getNivel());
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Ativo", "Inativo"));
        status.getSelectionModel().select(u.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("Nome:"), nome);
        grid.addRow(1, new Label("E-mail:"), email);
        grid.addRow(2, new Label("Nível:"), nivel);
        grid.addRow(3, new Label("Status:"), status);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                u.setNome(nome.getText().trim());
                u.setEmail(email.getText().trim());
                u.setNivel(nivel.getValue());
                u.setStatus(status.getValue());
                tableUsuarios.refresh();
                return u;
            }
            return null;
        });

        dialog.showAndWait();
    }

    /* ================== Modelo simples ================== */

    public static class User {
        private String nome;
        private String email;
        private String nivel;
        private String status;

        public User(String nome, String email, String nivel, String status) {
            this.nome = nome;
            this.email = email;
            this.nivel = nivel;
            this.status = status;
        }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNivel() { return nivel; }
        public void setNivel(String nivel) { this.nivel = nivel; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
