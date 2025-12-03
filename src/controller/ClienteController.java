package controller;

import database.ClienteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Cliente;

import java.util.List;
import java.util.Optional;

public class ClienteController {

    @FXML private TextField tfNome;
    @FXML private TextField tfCpf;
    @FXML private TextField tfEmail;
    @FXML private TextField tfRua;
    @FXML private TextField tfNumero;
    @FXML private TextField tfBairro;
    @FXML private TextField tfCidade;
    @FXML private TextField tfEstado;
    @FXML private TextField tfCep;
    @FXML private Button btnCadastrar;
    @FXML private Button btnLimpar;
    @FXML private TextField tfBusca;
    @FXML private Button btnAtualizar;
    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colEndereco;
    @FXML private TableColumn<Cliente, Void> colAcoes;

    private ObservableList<Cliente> masterList = FXCollections.observableArrayList();
    private ObservableList<Cliente> filteredList = FXCollections.observableArrayList();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private Cliente clienteEmEdicao = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEndereco.setCellValueFactory(new PropertyValueFactory<>("enderecoCompleto"));

        addActionsColumn();
        
        // Aplicar máscaras de formatação
        aplicarMascaraCPF(tfCpf);
        aplicarMascaraCEP(tfCep);
        
        // Estado: só letras, máximo 2 caracteres
        tfEstado.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > 2) {
                tfEstado.setText(oldV);
            }
            if (newV != null && !newV.matches("[a-zA-Z]*")) {
                tfEstado.setText(newV.replaceAll("[^a-zA-Z]", ""));
            }
        });
        
        // Número: só dígitos, máximo 5
        tfNumero.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.matches("\\d*")) {
                tfNumero.setText(newV.replaceAll("[^\\d]", ""));
            }
            if (newV != null && newV.length() > 5) {
                tfNumero.setText(oldV);
            }
        });

        carregarClientes();
        tfBusca.textProperty().addListener((obs, oldV, newV) -> filtrar());
    }

    // Aplica máscara de CPF enquanto o usuário digita: 000.000.000-00
    private void aplicarMascaraCPF(TextField textField) {
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }
            
            String apenasNumeros = newValue.replaceAll("[^\\d]", "");
            
            if (apenasNumeros.length() > 11) {
                apenasNumeros = apenasNumeros.substring(0, 11);
            }
            
            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < apenasNumeros.length(); i++) {
                if (i == 3 || i == 6) {
                    formatado.append(".");
                } else if (i == 9) {
                    formatado.append("-");
                }
                formatado.append(apenasNumeros.charAt(i));
            }
            
            if (!formatado.toString().equals(newValue)) {
                textField.setText(formatado.toString());
                textField.positionCaret(formatado.length());
            }
        });
    }

    // Aplica máscara de CEP enquanto o usuário digita: 00000-000
    private void aplicarMascaraCEP(TextField textField) {
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }
            
            String apenasNumeros = newValue.replaceAll("[^\\d]", "");
            
            if (apenasNumeros.length() > 8) {
                apenasNumeros = apenasNumeros.substring(0, 8);
            }
            
            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < apenasNumeros.length(); i++) {
                if (i == 5) {
                    formatado.append("-");
                }
                formatado.append(apenasNumeros.charAt(i));
            }
            
            if (!formatado.toString().equals(newValue)) {
                textField.setText(formatado.toString());
                textField.positionCaret(formatado.length());
            }
        });
    }

    // Valida CPF usando algoritmo oficial dos dígitos verificadores
    private boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^\\d]", "");
        
        if (cpf.length() != 11) {
            return false;
        }
        
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int digito1 = 11 - (soma % 11);
        if (digito1 > 9) digito1 = 0;
        
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int digito2 = 11 - (soma % 11);
        if (digito2 > 9) digito2 = 0;
        
        return Character.getNumericValue(cpf.charAt(9)) == digito1 &&
               Character.getNumericValue(cpf.charAt(10)) == digito2;
    }

    private void carregarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.getAll();
            
            if (clientes != null) {
                masterList.setAll(clientes);
                filteredList.setAll(clientes);
                tableClientes.setItems(filteredList);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao carregar clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCadastrarCliente() {
        String nome = tfNome.getText().trim();
        String cpf = tfCpf.getText().trim();
        String email = tfEmail.getText().trim();
        String rua = tfRua.getText().trim();
        String numero = tfNumero.getText().trim();
        String bairro = tfBairro.getText().trim();
        String cidade = tfCidade.getText().trim();
        String estado = tfEstado.getText().trim().toUpperCase();
        String cep = tfCep.getText().trim();

        if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Obrigatórios", "Nome, CPF e Email são obrigatórios!");
            return;
        }
        
        if (!validarCPF(cpf)) {
            showAlert(Alert.AlertType.WARNING, "CPF Inválido", "O CPF informado não é válido!");
            tfCpf.requestFocus();
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Email Inválido", "Digite um email válido!");
            tfEmail.requestFocus();
            return;
        }
        
        String cepNumeros = cep.replaceAll("[^\\d]", "");
        if (cepNumeros.length() != 8) {
            showAlert(Alert.AlertType.WARNING, "CEP Inválido", "O CEP deve ter 8 dígitos!");
            tfCep.requestFocus();
            return;
        }
        
        if (rua.isEmpty() || bairro.isEmpty() || cidade.isEmpty() || estado.isEmpty() || cep.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Endereço Incompleto", "Preencha todos os campos do endereço!");
            return;
        }
        
        if (estado.length() != 2) {
            showAlert(Alert.AlertType.WARNING, "Estado Inválido", "O estado deve ter 2 caracteres (ex: SP)");
            return;
        }

        try {
            String resultado;
            
            if (clienteEmEdicao != null) {
                clienteEmEdicao.setNome(nome);
                clienteEmEdicao.setCpf(cpf);
                clienteEmEdicao.setEmail(email);
                clienteEmEdicao.setRua(rua);
                clienteEmEdicao.setNumero(numero);
                clienteEmEdicao.setBairro(bairro);
                clienteEmEdicao.setCidade(cidade);
                clienteEmEdicao.setEstado(estado);
                clienteEmEdicao.setCep(cep);
                
                resultado = clienteDAO.atualizar(clienteEmEdicao);
                clienteEmEdicao = null;
                btnCadastrar.setText("Cadastrar Cliente");
                
            } else {
                Cliente novo = new Cliente(nome, email, cpf, rua, numero, bairro, cidade, estado, cep);
                resultado = clienteDAO.inserir(novo);
            }

            if (resultado.contains("sucesso")) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", resultado);
                limparFormulario();
                carregarClientes();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", resultado);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao salvar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onLimparFormulario() {
        limparFormulario();
    }

    @FXML
    private void onAtualizar() {
        carregarClientes();
    }

    private void limparFormulario() {
        tfNome.clear();
        tfCpf.clear();
        tfEmail.clear();
        tfRua.clear();
        tfNumero.clear();
        tfBairro.clear();
        tfCidade.clear();
        tfEstado.clear();
        tfCep.clear();
        clienteEmEdicao = null;
        btnCadastrar.setText("Cadastrar Cliente");
        tfNome.requestFocus();
    }

    private void filtrar() {
        String q = tfBusca.getText() == null ? "" : tfBusca.getText().toLowerCase().trim();

        if (q.isEmpty()) {
            filteredList.setAll(masterList);
        } else {
            ObservableList<Cliente> result = FXCollections.observableArrayList();
            for (Cliente c : masterList) {
                boolean match = (c.getNome() != null && c.getNome().toLowerCase().contains(q))
                             || (c.getCpf() != null && c.getCpf().toLowerCase().contains(q))
                             || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q));
                if (match) result.add(c);
            }
            filteredList.setAll(result);
        }
        
        tableClientes.setItems(filteredList);
    }

    private void addActionsColumn() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-cursor: hand;");

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
    }

    private void editarCliente(Cliente c) {
        clienteEmEdicao = c;
        
        tfNome.setText(c.getNome());
        tfCpf.setText(c.getCpf());
        tfEmail.setText(c.getEmail());
        tfRua.setText(c.getRua());
        tfNumero.setText(c.getNumero());
        tfBairro.setText(c.getBairro());
        tfCidade.setText(c.getCidade());
        tfEstado.setText(c.getEstado());
        tfCep.setText(c.getCep());
        
        btnCadastrar.setText("Atualizar Cliente");
        tfNome.requestFocus();
    }

    private void excluirCliente(Cliente c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Excluir cliente?");
        confirm.setContentText("Deseja realmente excluir " + c.getNome() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String resultado = clienteDAO.deletar(c.getId());
                
                if (resultado.contains("sucesso")) {
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", resultado);
                    carregarClientes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro", resultado);
                }
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir cliente: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}