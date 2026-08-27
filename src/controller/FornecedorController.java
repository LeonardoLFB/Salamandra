package controller;

import database.FornecedorDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import model.Fornecedor;

import java.util.List;
import java.util.Optional;

public class FornecedorController {

    private static final String STATUS_ATIVO = "Ativo";
    private static final String STATUS_INATIVO = "Inativo";
    private static final String FILTRO_TODOS = "Todos";

    @FXML private TextField tfNome;
    @FXML private TextField tfCnpj;
    @FXML private TextField tfContato;
    @FXML private TextField tfEmail;
    @FXML private TextField tfEndereco;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblFormTitulo;
    @FXML private Label lblModoEdicao;
    @FXML private Button btnCancelarEdicao;
    @FXML private Button btnLimpar;
    @FXML private Button btnCadastrar;

    @FXML private TextField tfBusca;
    @FXML private ComboBox<String> cbStatusFiltro;
    @FXML private Button btnAtualizar;

    @FXML private TableView<Fornecedor> tableFornecedores;
    @FXML private TableColumn<Fornecedor, String> colNome;
    @FXML private TableColumn<Fornecedor, String> colCnpj;
    @FXML private TableColumn<Fornecedor, String> colContato;
    @FXML private TableColumn<Fornecedor, String> colEmail;
    @FXML private TableColumn<Fornecedor, String> colStatus;
    @FXML private TableColumn<Fornecedor, Void> colAcoes;

    @FXML private Label lblTotal;
    @FXML private Label lblAtivos;
    @FXML private Label lblStatusBar;
    @FXML private Label lblExibindo;

    private final ObservableList<Fornecedor> masterList = FXCollections.observableArrayList();
    private final ObservableList<Fornecedor> filteredList = FXCollections.observableArrayList();
    private final FornecedorDAO fornecedorDAO = new FornecedorDAO();
    private Fornecedor fornecedorEmEdicao = null;

    @FXML
    public void initialize() {
        configurarColunas();
        addActionsColumn();

        tableFornecedores.setItems(filteredList);
        tableFornecedores.setPlaceholder(new Label("Nenhum fornecedor cadastrado."));

        cbStatus.getItems().addAll(STATUS_ATIVO, STATUS_INATIVO);
        cbStatus.getSelectionModel().select(STATUS_ATIVO);

        cbStatusFiltro.getItems().addAll(FILTRO_TODOS, STATUS_ATIVO, STATUS_INATIVO);
        cbStatusFiltro.getSelectionModel().select(FILTRO_TODOS);
        cbStatusFiltro.setOnAction(e -> filtrar());

        aplicarMascaraCnpjCpf(tfCnpj);
        aplicarMascaraTelefone(tfContato);

        // Duplo clique numa linha abre o fornecedor para edicao
        tableFornecedores.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Fornecedor f = tableFornecedores.getSelectionModel().getSelectedItem();
                if (f != null) editarFornecedor(f);
            }
        });

        tfBusca.textProperty().addListener((obs, oldV, newV) -> filtrar());

        carregarFornecedores();
    }

    private void configurarColunas() {
        // Usa lambdas em vez de PropertyValueFactory: o model nao expoe
        // JavaFX properties e assim evitamos reflexao por nome de campo.
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCnpj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCnpjCpf()));
        colContato.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContato()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Status renderizado como badge colorido
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null || status.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(STATUS_ATIVO.equals(status) ? "badge-ativo" : "badge-inativo");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void carregarFornecedores() {
        try {
            List<Fornecedor> fornecedores = fornecedorDAO.getAll();

            if (fornecedores != null) {
                masterList.setAll(fornecedores);
                filtrar();
                lblStatusBar.setText("Fornecedores carregados: " + fornecedores.size());
            }
        } catch (Exception e) {
            lblStatusBar.setText("Erro ao carregar fornecedores");
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao carregar fornecedores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCadastrarFornecedor() {
        String nome = tfNome.getText().trim();
        String cnpjCpf = tfCnpj.getText().trim();
        String contato = tfContato.getText().trim();
        String email = tfEmail.getText().trim();
        String endereco = tfEndereco.getText().trim();
        String status = cbStatus.getValue() == null ? STATUS_ATIVO : cbStatus.getValue();

        if (nome.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo Obrigatório", "O nome do fornecedor é obrigatório!");
            tfNome.requestFocus();
            return;
        }

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "E-mail Inválido", "Digite um e-mail válido!");
            tfEmail.requestFocus();
            return;
        }

        String docNumeros = cnpjCpf.replaceAll("[^\\d]", "");
        if (!docNumeros.isEmpty() && docNumeros.length() != 11 && docNumeros.length() != 14) {
            showAlert(Alert.AlertType.WARNING, "Documento Inválido",
                    "O CNPJ deve ter 14 dígitos e o CPF, 11 dígitos.");
            tfCnpj.requestFocus();
            return;
        }

        try {
            String resultado;

            if (fornecedorEmEdicao != null) {
                fornecedorEmEdicao.setNome(nome);
                fornecedorEmEdicao.setCnpjCpf(cnpjCpf);
                fornecedorEmEdicao.setContato(contato);
                fornecedorEmEdicao.setEmail(email);
                fornecedorEmEdicao.setEndereco(endereco);
                fornecedorEmEdicao.setStatus(status);

                resultado = fornecedorDAO.atualizar(fornecedorEmEdicao);
            } else {
                Fornecedor novo = new Fornecedor(nome, cnpjCpf, contato, email, endereco, status);
                resultado = fornecedorDAO.inserir(novo);
            }

            if (resultado.contains("sucesso")) {
                lblStatusBar.setText(resultado);
                limparFormulario();
                carregarFornecedores();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", resultado);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao salvar fornecedor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onLimparFormulario() {
        limparFormulario();
    }

    @FXML
    private void onAtualizar() {
        carregarFornecedores();
    }

    private void limparFormulario() {
        tfNome.clear();
        tfCnpj.clear();
        tfContato.clear();
        tfEmail.clear();
        tfEndereco.clear();
        cbStatus.getSelectionModel().select(STATUS_ATIVO);
        fornecedorEmEdicao = null;
        tableFornecedores.getSelectionModel().clearSelection();
        alternarModoEdicao(false);
        tfNome.requestFocus();
    }

    /** Alterna o formulario entre "novo cadastro" e "edicao". */
    private void alternarModoEdicao(boolean editando) {
        lblFormTitulo.setText(editando ? "Editar Fornecedor" : "Cadastrar Novo Fornecedor");
        btnCadastrar.setText(editando ? "Salvar Alterações" : "Cadastrar Fornecedor");

        lblModoEdicao.setVisible(editando);
        lblModoEdicao.setManaged(editando);
        btnCancelarEdicao.setVisible(editando);
        btnCancelarEdicao.setManaged(editando);
        btnLimpar.setVisible(!editando);
        btnLimpar.setManaged(!editando);
    }

    private void filtrar() {
        String q = tfBusca.getText() == null ? "" : tfBusca.getText().toLowerCase().trim();
        String statusFiltro = cbStatusFiltro.getValue();

        ObservableList<Fornecedor> result = FXCollections.observableArrayList();
        for (Fornecedor f : masterList) {
            boolean matchBusca = q.isEmpty()
                    || contem(f.getNome(), q)
                    || contem(f.getCnpjCpf(), q)
                    || contem(f.getContato(), q)
                    || contem(f.getEmail(), q);

            boolean matchStatus = statusFiltro == null || FILTRO_TODOS.equals(statusFiltro)
                    || (f.getStatus() != null && f.getStatus().equals(statusFiltro));

            if (matchBusca && matchStatus) {
                result.add(f);
            }
        }
        filteredList.setAll(result);
        atualizarResumo();
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase().contains(termo);
    }

    private void atualizarResumo() {
        int ativos = 0;
        for (Fornecedor f : masterList) {
            if (STATUS_ATIVO.equals(f.getStatus())) ativos++;
        }
        lblTotal.setText(String.valueOf(masterList.size()));
        lblAtivos.setText(String.valueOf(ativos));
        lblExibindo.setText("Exibindo " + filteredList.size() + " de " + masterList.size());
    }

    private void addActionsColumn() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("icon-btn");
                btnDelete.getStyleClass().add("icon-btn");
                btnEdit.setTooltip(new Tooltip("Editar fornecedor"));
                btnDelete.setTooltip(new Tooltip("Excluir fornecedor"));

                btnEdit.setOnAction(e -> {
                    Fornecedor f = getTableView().getItems().get(getIndex());
                    if (f != null) editarFornecedor(f);
                });

                btnDelete.setOnAction(e -> {
                    Fornecedor f = getTableView().getItems().get(getIndex());
                    if (f != null) excluirFornecedor(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void editarFornecedor(Fornecedor f) {
        fornecedorEmEdicao = f;

        tfNome.setText(f.getNome());
        tfCnpj.setText(f.getCnpjCpf());
        tfContato.setText(f.getContato());
        tfEmail.setText(f.getEmail());
        tfEndereco.setText(f.getEndereco());
        cbStatus.getSelectionModel().select(
                f.getStatus() == null ? STATUS_ATIVO : f.getStatus());

        alternarModoEdicao(true);
        lblStatusBar.setText("Editando: " + f.getNome());
        tfNome.requestFocus();
    }

    private void excluirFornecedor(Fornecedor f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Excluir fornecedor?");
        confirm.setContentText("Deseja realmente excluir " + f.getNome() + "?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String resultado = fornecedorDAO.deletar(f.getId());

                if (resultado.contains("sucesso")) {
                    lblStatusBar.setText(resultado);
                    if (fornecedorEmEdicao != null && fornecedorEmEdicao.getId() != null
                            && fornecedorEmEdicao.getId().equals(f.getId())) {
                        limparFormulario();
                    }
                    carregarFornecedores();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro", resultado);
                }

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir fornecedor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /** Formata CPF (000.000.000-00) ou CNPJ (00.000.000/0000-00) conforme digita. */
    private void aplicarMascaraCnpjCpf(TextField campo) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String d = newValue.replaceAll("[^\\d]", "");
            if (d.length() > 14) d = d.substring(0, 14);

            StringBuilder sb = new StringBuilder();
            if (d.length() <= 11) { // CPF
                for (int i = 0; i < d.length(); i++) {
                    if (i == 3 || i == 6) sb.append('.');
                    else if (i == 9) sb.append('-');
                    sb.append(d.charAt(i));
                }
            } else { // CNPJ
                for (int i = 0; i < d.length(); i++) {
                    if (i == 2 || i == 5) sb.append('.');
                    else if (i == 8) sb.append('/');
                    else if (i == 12) sb.append('-');
                    sb.append(d.charAt(i));
                }
            }

            if (!sb.toString().equals(newValue)) {
                campo.setText(sb.toString());
                campo.positionCaret(sb.length());
            }
        });
    }

    /** Formata telefone (00) 0000-0000 ou (00) 00000-0000 conforme digita. */
    private void aplicarMascaraTelefone(TextField campo) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String d = newValue.replaceAll("[^\\d]", "");
            if (d.length() > 11) d = d.substring(0, 11);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < d.length(); i++) {
                if (i == 0) sb.append('(');
                if (i == 2) sb.append(") ");
                if (i == (d.length() > 10 ? 7 : 6)) sb.append('-');
                sb.append(d.charAt(i));
            }

            if (!sb.toString().equals(newValue)) {
                campo.setText(sb.toString());
                campo.positionCaret(sb.length());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
