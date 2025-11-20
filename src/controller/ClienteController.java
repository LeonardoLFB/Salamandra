package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Cliente;

import java.util.Optional;

/**
 * Controller para Clientes.fxml
 *
 * Requisitos do modelo (model.Cliente):
 * - getters: getId(), getNome(), getCpfCnpj(), getContato(), getEmail(), getStatus()
 * - setters correspondentes se quiser persistir edição
 *
 * Salve este arquivo em src/controller/ClienteController.java
 */
public class ClienteController {

    // Formulário
    @FXML private TextField tfNome;
    @FXML private TextField tfCpfCnpj;
    @FXML private TextField tfContato;
    @FXML private TextField tfEmail;
    @FXML private TextField tfEndereco;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnCadastrar;

    // Busca / filtros
    @FXML private TextField tfBusca;
    @FXML private ComboBox<String> cbStatusFiltro;
    @FXML private Button btnNovo;

    // Tabela
    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, String> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colContato;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colStatus;
    @FXML private TableColumn<Cliente, Void> colAcoes;

    // Dados em memória (substitua por DAO/BD quando integrar)
    private final ObservableList<Cliente> masterList = FXCollections.observableArrayList();

    // contador simples para gerar IDs (string com #)
    private int idCounter = 1000;

    @FXML
    public void initialize() {
        // Mapear colunas com nomes de propriedades (getters do model.Cliente)
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpfCnpj"));
        colContato.setCellValueFactory(new PropertyValueFactory<>("contato"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Badge visual para Status
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label lbl = new Label();
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    lbl.setText(status);
                    lbl.setStyle("-fx-padding: 4 10; -fx-background-radius: 16; -fx-font-size: 12;");
                    String s = status.toLowerCase();
                    if (s.contains("ativo")) {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #dff3e6; -fx-text-fill: #1f7a3a;");
                    } else {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #ffe6e9; -fx-text-fill: #a21d2b;");
                    }
                    setGraphic(lbl);
                }
            }
        });

        // Coluna de Ações (editar / excluir)
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-font-size: 14;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-font-size: 14;");

                btnEdit.setOnAction(e -> {
                    Cliente c = getTableView().getItems().get(getIndex());
                    if (c != null) editarCliente(c);
                });

                btnDelete.setOnAction(e -> {
                    Cliente c = getTableView().getItems().get(getIndex());
                    if (c != null) excluirCliente(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Dados de exemplo
        masterList.addAll(
                new Cliente(generateId(), "João da Silva", "123.456.789-00", "(11) 98765-4321", "joao@mail.com", "Ativo"),
                new Cliente(generateId(), "Maria Oliveira", "12.345.678/0001-90", "(21) 91234-5678", "maria@mail.com", "Ativo"),
                new Cliente(generateId(), "Carlos Pereira", "987.654.321-00", "(31) 99999-8888", "carlos@mail.com", "Inativo")
        );

        tableClientes.setItems(FXCollections.observableArrayList(masterList));

        // Configurar cbStatus (form) e cbStatusFiltro (filtro)
        cbStatus.getItems().addAll("Ativo", "Inativo");
        cbStatus.getSelectionModel().select("Ativo");

        cbStatusFiltro.getItems().addAll("Todos", "Ativo", "Inativo");
        cbStatusFiltro.getSelectionModel().select("Todos");
        cbStatusFiltro.setOnAction(e -> filtrar());

        // Busca em tempo real
        tfBusca.textProperty().addListener((obs, oldV, newV) -> filtrar());
    }

    // botão cadastrar (onAction="#onCadastrarCliente")
    @FXML
    private void onCadastrarCliente() {
        String nome = tfNome.getText() == null ? "" : tfNome.getText().trim();
        String cpf = tfCpfCnpj.getText() == null ? "" : tfCpfCnpj.getText().trim();
        String contato = tfContato.getText() == null ? "" : tfContato.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        String status = cbStatus.getValue() == null ? "Ativo" : cbStatus.getValue();

        if (nome.isEmpty() || cpf.isEmpty()) {
            alert("Nome e CPF/CNPJ são obrigatórios.");
            return;
        }

        Cliente novo = new Cliente(generateId(), nome, cpf, contato, email, status);
        masterList.add(novo);
        limparFormulario();
        filtrar();
    }

    // botão "+ Novo Cliente" — limpa e foca
    @FXML
    private void onAbrirFormNovo() {
        limparFormulario();
        tfNome.requestFocus();
    }

    // editar: preenche o formulário e remove temporariamente da lista
    private void editarCliente(Cliente c) {
        tfNome.setText(c.getNome());
        tfCpfCnpj.setText(c.getCpfCnpj());
        tfContato.setText(c.getContato());
        tfEmail.setText(c.getEmail());
        cbStatus.setValue(c.getStatus());

        // remove para que, ao salvar, o item seja re-adicionado (estratégia simples)
        masterList.remove(c);
        filtrar();
    }

    // excluir com confirmação
    private void excluirCliente(Cliente c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar exclusão");
        a.setHeaderText("Excluir cliente?");
        a.setContentText("Deseja realmente excluir " + c.getNome() + "?");
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            masterList.remove(c);
            filtrar();
        }
    }

    // filtra por busca e status
    private void filtrar() {
        String q = tfBusca.getText() == null ? "" : tfBusca.getText().toLowerCase().trim();
        String statusFiltro = cbStatusFiltro.getValue() == null ? "Todos" : cbStatusFiltro.getValue();

        ObservableList<Cliente> shown = FXCollections.observableArrayList();
        for (Cliente c : masterList) {
            boolean matchBusca = q.isEmpty()
                    || (c.getNome() != null && c.getNome().toLowerCase().contains(q))
                    || (c.getCpfCnpj() != null && c.getCpfCnpj().toLowerCase().contains(q))
                    || (c.getId() != null && c.getId().toLowerCase().contains(q));
            boolean matchStatus = statusFiltro.equals("Todos") || (c.getStatus() != null && c.getStatus().equalsIgnoreCase(statusFiltro));
            if (matchBusca && matchStatus) shown.add(c);
        }
        tableClientes.setItems(shown);
    }

    private void limparFormulario() {
        tfNome.clear();
        tfCpfCnpj.clear();
        tfContato.clear();
        tfEmail.clear();
        tfEndereco.clear();
        cbStatus.getSelectionModel().select("Ativo");
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private String generateId() {
        idCounter++;
        return "#C" + idCounter;
    }
}
