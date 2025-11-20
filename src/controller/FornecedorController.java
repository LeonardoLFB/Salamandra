package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Fornecedor;

import java.util.Optional;

public class FornecedorController {

    // Campos do formulário
    @FXML private TextField tfNome;
    @FXML private TextField tfCnpj;
    @FXML private TextField tfContato;
    @FXML private TextField tfEmail;
    @FXML private TextField tfEndereco;
    @FXML private Button btnCadastrar;

    // Filtros / busca
    @FXML private TextField tfBusca;
    @FXML private ComboBox<String> cbStatusFiltro;
    @FXML private Button btnNovo;

    // Tabela
    @FXML private TableView<Fornecedor> tableFornecedores;
    @FXML private TableColumn<Fornecedor, String> colNome;
    @FXML private TableColumn<Fornecedor, String> colCnpj;
    @FXML private TableColumn<Fornecedor, String> colContato;
    @FXML private TableColumn<Fornecedor, String> colStatus;
    @FXML private TableColumn<Fornecedor, Void> colAcoes;

    private final ObservableList<Fornecedor> fornecedores = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // Configurar colunas
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpjCpf"));
        colContato.setCellValueFactory(new PropertyValueFactory<>("contato"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Badge de status
        colStatus.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }

                badge.setText(status);
                badge.setStyle("-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 12;");

                if (status.equalsIgnoreCase("Ativo")) {
                    badge.setStyle(badge.getStyle()
                            + "-fx-background-color: #dff3e6; -fx-text-fill: #1f7a3a;");
                } else {
                    badge.setStyle(badge.getStyle()
                            + "-fx-background-color: #ffe6e9; -fx-text-fill: #a21d2b;");
                }

                setGraphic(badge);
            }
        });

        // Coluna de ações (editar e excluir)
        colAcoes.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-font-size: 14;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-font-size: 14;");

                btnEdit.setOnAction(event -> editar(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> excluir(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Dados de exemplo
        fornecedores.addAll(
                new Fornecedor("Incensos do Oriente Ltda", "12.345.678/0001-90", "(11) 98765-4321", "contato@incensos.com", "Rua das Especiarias, SP", "Ativo"),
                new Fornecedor("Aromas da Terra", "98.765.432/0001-10", "(21) 91234-5678", "aromas@gmail.com", "Av. das Flores, RJ", "Ativo"),
                new Fornecedor("Essências Pura Magia", "123.456.789-00", "(31) 99999-8888", "pura@magia.com", "Rua Luz, MG", "Inativo")
        );

        tableFornecedores.setItems(fornecedores);

        // Configurar filtro de status
        cbStatusFiltro.getItems().addAll("Todos", "Ativo", "Inativo");
        cbStatusFiltro.getSelectionModel().select("Todos");
        cbStatusFiltro.setOnAction(e -> filtrar());

        // Busca em tempo real
        tfBusca.textProperty().addListener((obs, oldV, newV) -> filtrar());
    }

    // Cadastro
    @FXML
    private void onCadastrarFornecedor() {
        if (tfNome.getText().isBlank() || tfCnpj.getText().isBlank()) {
            alert("Preencha Nome e CNPJ/CPF para cadastrar.");
            return;
        }

        fornecedores.add(
                new Fornecedor(
                        tfNome.getText(),
                        tfCnpj.getText(),
                        tfContato.getText(),
                        tfEmail.getText(),
                        tfEndereco.getText(),
                        "Ativo"
                )
        );

        limparCampos();
        filtrar();
    }

    // Novo
    @FXML
    private void onAbrirFormNovo() {
        limparCampos();
        tfNome.requestFocus();
    }

    // Edição
    private void editar(Fornecedor f) {
        tfNome.setText(f.getNome());
        tfCnpj.setText(f.getCnpjCpf());
        tfContato.setText(f.getContato());
        tfEmail.setText(f.getEmail());
        tfEndereco.setText(f.getEndereco());

        fornecedores.remove(f); // remove temporariamente para sobrescrever
    }

    // Exclusão
    private void excluir(Fornecedor f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Excluir fornecedor?");
        confirm.setContentText("Fornecedor: " + f.getNome());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            fornecedores.remove(f);
            filtrar();
        }
    }

    // Filtro
    private void filtrar() {
        String busca = tfBusca.getText().toLowerCase().trim();
        String statusFiltro = cbStatusFiltro.getValue();

        ObservableList<Fornecedor> filtrados = FXCollections.observableArrayList();

        for (Fornecedor f : fornecedores) {

            boolean matchBusca =
                    busca.isEmpty()
                            || f.getNome().toLowerCase().contains(busca)
                            || f.getCnpjCpf().toLowerCase().contains(busca);

            boolean matchStatus =
                    statusFiltro.equals("Todos")
                            || ((String) f.getStatus()).equalsIgnoreCase(statusFiltro);

            if (matchBusca && matchStatus) {
                filtrados.add(f);
            }
        }

        tableFornecedores.setItems(filtrados);
    }

    // Utilitários
    private void limparCampos() {
        tfNome.clear();
        tfCnpj.clear();
        tfContato.clear();
        tfEmail.clear();
        tfEndereco.clear();
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}
