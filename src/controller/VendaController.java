package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import database.ClienteDAO;
import database.ProdutoDAO;
import database.VendaDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Cliente;
import model.ItemVenda;
import model.Produto;
import model.Venda;

public class VendaController {

    // ==================== CAMPOS DO FORMULÁRIO ====================
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Produto> comboProduto;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPrecoUnitario;
    @FXML private DatePicker datePickerVenda;
    @FXML private ComboBox<String> comboPagamento;
    
    // ==================== TABELA DE ITENS ====================
    @FXML private TableView<ItemVenda> tableItens;
    @FXML private TableColumn<ItemVenda, String> colItemProduto;
    @FXML private TableColumn<ItemVenda, Integer> colItemQuantidade;
    @FXML private TableColumn<ItemVenda, String> colItemPreco;
    @FXML private TableColumn<ItemVenda, String> colItemSubtotal;
    @FXML private TableColumn<ItemVenda, Void> colItemAcoes;
    
    @FXML private Label lblValorTotal;
    
    // ==================== BUSCA E FILTRO ====================
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> comboFiltroStatus;
    
    // ==================== TABELA DE VENDAS ====================
    @FXML private TableView<Venda> tableVendas;
    @FXML private TableColumn<Venda, Integer> colId;
    @FXML private TableColumn<Venda, String> colCliente;
    @FXML private TableColumn<Venda, String> colData;
    @FXML private TableColumn<Venda, String> colValor;
    @FXML private TableColumn<Venda, String> colStatus;
    @FXML private TableColumn<Venda, Void> colAcoes;
    
    // ==================== LISTAS OBSERVÁVEIS ====================
    private ObservableList<Cliente> todosClientes = FXCollections.observableArrayList();
    private ObservableList<Produto> todosProdutos = FXCollections.observableArrayList();
    private ObservableList<ItemVenda> itensVenda = FXCollections.observableArrayList();
    private ObservableList<Venda> todasVendas = FXCollections.observableArrayList();
    private ObservableList<Venda> vendasFiltradas = FXCollections.observableArrayList();
    
    // ==================== DAOs ====================
    private ClienteDAO clienteDAO = new ClienteDAO();
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private VendaDAO vendaDAO = new VendaDAO();
    
    // ==================== VARIÁVEIS DE CONTROLE ====================
    private double valorTotalVenda = 0.0;
    private String formaPagamentoSelecionada = "Dinheiro";

    @FXML
    public void initialize() {
        configurarComboCliente();
        configurarComboProduto();
        configurarComboPagamento();
        configurarComboFiltroStatus();
        configurarTabelaItens();
        configurarTabelaVendas();
        carregarDadosIniciais();
        
        // Define data padrão como hoje
        datePickerVenda.setValue(LocalDate.now());
    }

    // ==================== CONFIGURAÇÃO DOS COMBOS ====================
    
    private void configurarComboCliente() {
        // Carregar todos os clientes do banco
        todosClientes.setAll(clienteDAO.getAll());
        comboCliente.setItems(todosClientes);
        
        // Configurar exibição (mostra o nome do cliente)
        comboCliente.setConverter(new javafx.util.StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente != null ? cliente.getNome() : "";
            }
            
            @Override
            public Cliente fromString(String string) {
                return todosClientes.stream()
                    .filter(c -> c.getNome().equals(string))
                    .findFirst()
                    .orElse(null);
            }
        });
        
        // Configurar comportamento quando o campo é editado
        comboCliente.setOnMouseClicked(e -> {
            if (!comboCliente.isShowing()) {
                comboCliente.setItems(todosClientes);
            }
        });
        
        // Filtro em tempo real conforme digita
        comboCliente.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (comboCliente.isShowing()) {
                return; // Não filtrar enquanto o dropdown está aberto
            }
            
            if (newVal == null || newVal.isEmpty()) {
                comboCliente.setItems(todosClientes);
            } else {
                ObservableList<Cliente> filtrados = todosClientes.stream()
                    .filter(c -> c.getNome().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
                
                if (filtrados.isEmpty()) {
                    comboCliente.setItems(todosClientes);
                } else {
                    comboCliente.setItems(filtrados);
                }
            }
        });
    }
    
    private void configurarComboProduto() {
        // Carregar todos os produtos do banco
        todosProdutos.setAll(produtoDAO.getAll());
        comboProduto.setItems(todosProdutos);
        
        // Configurar exibição (mostra nome e código)
        comboProduto.setConverter(new javafx.util.StringConverter<Produto>() {
            @Override
            public String toString(Produto produto) {
                return produto != null ? produto.getNome() + " (Cód: " + produto.getCodigo() + ")" : "";
            }
            
            @Override
            public Produto fromString(String string) {
                return todosProdutos.stream()
                    .filter(p -> (p.getNome() + " (Cód: " + p.getCodigo() + ")").equals(string))
                    .findFirst()
                    .orElse(null);
            }
        });
        
        // Configurar comportamento quando o campo é clicado
        comboProduto.setOnMouseClicked(e -> {
            if (!comboProduto.isShowing()) {
                comboProduto.setItems(todosProdutos);
            }
        });
        
        // Filtro em tempo real
        comboProduto.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (comboProduto.isShowing()) {
                return; // Não filtrar enquanto o dropdown está aberto
            }
            
            if (newVal == null || newVal.isEmpty()) {
                comboProduto.setItems(todosProdutos);
            } else {
                ObservableList<Produto> filtrados = todosProdutos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(newVal.toLowerCase()) ||
                               String.valueOf(p.getCodigo()).contains(newVal))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
                
                if (filtrados.isEmpty()) {
                    comboProduto.setItems(todosProdutos);
                } else {
                    comboProduto.setItems(filtrados);
                }
            }
        });
        
        // Quando seleciona um produto, preenche o preço automaticamente
        comboProduto.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtPrecoUnitario.setText(String.format("%.2f", newVal.getPrecoVenda()));
            }
        });
    }
    
    private void configurarComboPagamento() {
        comboPagamento.setItems(FXCollections.observableArrayList(
            "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Pix", "Transferência"
        ));
        comboPagamento.setValue("Dinheiro");
        
        comboPagamento.setOnAction(e -> {
            formaPagamentoSelecionada = comboPagamento.getValue();
        });
    }
    
    private void configurarComboFiltroStatus() {
        comboFiltroStatus.setItems(FXCollections.observableArrayList(
            "Todos", "Pendente", "Concluída", "Cancelada"
        ));
        comboFiltroStatus.setValue("Todos");
    }

    // ==================== CONFIGURAÇÃO DA TABELA DE ITENS ====================
    
    private void configurarTabelaItens() {
        colItemProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colItemQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        
        colItemPreco.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getPrecoUnitario()))
        );
        
        colItemSubtotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getSubtotal()))
        );
        
        // Coluna de ações (botão remover)
        colItemAcoes.setCellFactory(col -> new TableCell<ItemVenda, Void>() {
            private final Button btnRemover = new Button("Remover");
            
            {
                btnRemover.setStyle("-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-size:10px; -fx-padding:4 8;");
                btnRemover.setOnAction(e -> {
                    ItemVenda item = getTableView().getItems().get(getIndex());
                    itensVenda.remove(item);
                    atualizarValorTotal();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemover);
            }
        });
        
        tableItens.setItems(itensVenda);
    }

    // ==================== CONFIGURAÇÃO DA TABELA DE VENDAS ====================
    
    private void configurarTabelaVendas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idVenda"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colData.setCellValueFactory(new PropertyValueFactory<>("dataFormatada"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorFormatado"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Coluna de ações (botões Ver Detalhes, Concluir, Cancelar)
        colAcoes.setCellFactory(col -> new TableCell<Venda, Void>() {
            private final Button btnDetalhes = new Button("Ver");
            private final Button btnConcluir = new Button("Concluir");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox hbox = new HBox(5, btnDetalhes, btnConcluir, btnCancelar);
            
            {
                btnDetalhes.setStyle("-fx-background-color:#17a2b8; -fx-text-fill:white; -fx-font-size:10px; -fx-padding:4 8;");
                btnConcluir.setStyle("-fx-background-color:#28a745; -fx-text-fill:white; -fx-font-size:10px; -fx-padding:4 8;");
                btnCancelar.setStyle("-fx-background-color:#dc3545; -fx-text-fill:white; -fx-font-size:10px; -fx-padding:4 8;");
                
                btnDetalhes.setOnAction(e -> {
                    Venda venda = getTableView().getItems().get(getIndex());
                    mostrarDetalhesVenda(venda);
                });
                
                btnConcluir.setOnAction(e -> {
                    Venda venda = getTableView().getItems().get(getIndex());
                    concluirVenda(venda);
                });
                
                btnCancelar.setOnAction(e -> {
                    Venda venda = getTableView().getItems().get(getIndex());
                    cancelarVenda(venda);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Venda venda = getTableView().getItems().get(getIndex());
                    
                    // Mostrar apenas botões relevantes baseado no status
                    if ("Concluída".equals(venda.getStatus()) || "Cancelada".equals(venda.getStatus())) {
                        setGraphic(new HBox(5, btnDetalhes));
                    } else {
                        setGraphic(hbox);
                    }
                }
            }
        });
        
        tableVendas.setItems(vendasFiltradas);
    }

    // ==================== CARREGAR DADOS INICIAIS ====================
    
    private void carregarDadosIniciais() {
        todasVendas.setAll(vendaDAO.getAll());
        vendasFiltradas.setAll(todasVendas);

        // Recarrega os produtos para refletir o estoque atualizado
        // (a venda dá baixa e o cancelamento devolve as quantidades)
        todosProdutos.setAll(produtoDAO.getAll());
    }

    // ==================== ADICIONAR ITEM À VENDA ====================
    
    @FXML
    private void onAdicionarItem(ActionEvent event) {
        // Validações
        Cliente clienteSelecionado = comboCliente.getValue();
        Produto produtoSelecionado = comboProduto.getValue();
        
        if (clienteSelecionado == null) {
            mostrarErro("Cliente não selecionado", "Por favor, selecione um cliente válido.");
            return;
        }
        
        if (produtoSelecionado == null) {
            mostrarErro("Produto não selecionado", "Por favor, selecione um produto válido.");
            return;
        }
        
        // Validar quantidade
        int quantidade;
        try {
            quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            if (quantidade <= 0) {
                mostrarErro("Quantidade inválida", "A quantidade deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarErro("Quantidade inválida", "Digite um número válido para a quantidade.");
            return;
        }
        
        // Validar estoque
        if (quantidade > produtoSelecionado.getQtdeEstoque()) {
            mostrarErro("Estoque insuficiente", 
                "Estoque disponível: " + produtoSelecionado.getQtdeEstoque() + " unidades.");
            return;
        }
        
        // Validar preço
        double precoUnitario;
        try {
            precoUnitario = Double.parseDouble(txtPrecoUnitario.getText().replace(",", ".").trim());
            if (precoUnitario <= 0) {
                mostrarErro("Preço inválido", "O preço deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarErro("Preço inválido", "Digite um valor válido para o preço.");
            return;
        }
        
        // Criar item
        ItemVenda item = new ItemVenda();
        item.setIdProduto(produtoSelecionado.getId());
        item.setNomeProduto(produtoSelecionado.getNome());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        item.calcularSubtotal();
        
        // Verificar se produto já foi adicionado
        Optional<ItemVenda> itemExistente = itensVenda.stream()
            .filter(i -> i.getIdProduto() == produtoSelecionado.getId())
            .findFirst();
        
        if (itemExistente.isPresent()) {
            // Atualizar quantidade
            ItemVenda itemAtualizar = itemExistente.get();
            int novaQuantidade = itemAtualizar.getQuantidade() + quantidade;
            
            if (novaQuantidade > produtoSelecionado.getQtdeEstoque()) {
                mostrarErro("Estoque insuficiente", 
                    "Quantidade total excede o estoque disponível.");
                return;
            }
            
            itemAtualizar.setQuantidade(novaQuantidade);
            itemAtualizar.calcularSubtotal();
            tableItens.refresh();
        } else {
            // Adicionar novo item
            itensVenda.add(item);
        }
        
        // Atualizar valor total
        atualizarValorTotal();
        
        // Limpar campos do produto
        comboProduto.setValue(null);
        comboProduto.getEditor().clear();
        txtQuantidade.clear();
        txtPrecoUnitario.clear();
        
        mostrarSucesso("Item adicionado com sucesso!");
    }

    // ==================== FINALIZAR VENDA ====================
    
    @FXML
    private void onFinalizarVenda(ActionEvent event) {
        // Validações
        Cliente clienteSelecionado = comboCliente.getValue();
        
        if (clienteSelecionado == null) {
            mostrarErro("Cliente não selecionado", "Por favor, selecione um cliente válido.");
            return;
        }
        
        if (itensVenda.isEmpty()) {
            mostrarErro("Venda sem itens", "Adicione pelo menos um item à venda.");
            return;
        }
        
        LocalDate data = datePickerVenda.getValue();
        if (data == null) {
            mostrarErro("Data não selecionada", "Por favor, selecione a data da venda.");
            return;
        }
        
        // Criar venda
        Venda venda = new Venda();
        venda.setIdCliente(clienteSelecionado.getId());
        venda.setData(LocalDateTime.of(data, LocalTime.now()));
        venda.setValorTotal(valorTotalVenda);
        venda.setStatus("Pendente");
        venda.setObservacao(formaPagamentoSelecionada);
        
        // Criar lista de itens
        List<ItemVenda> itens = new ArrayList<>(itensVenda);
        
        // Salvar no banco
        String resultado = vendaDAO.inserir(venda, itens);
        
        if (resultado.contains("sucesso")) {
            mostrarSucesso("Venda registrada com sucesso!\nID da venda: " + venda.getIdVenda());
            limparFormulario();
            carregarDadosIniciais();
        } else {
            mostrarErro("Erro ao salvar venda", resultado);
        }
    }

    // ==================== LIMPAR FORMULÁRIO ====================
    
    @FXML
    private void onLimpar(ActionEvent event) {
        limparFormulario();
    }
    
    private void limparFormulario() {
        comboCliente.setValue(null);
        comboCliente.getEditor().clear();
        comboProduto.setValue(null);
        comboProduto.getEditor().clear();
        txtQuantidade.clear();
        txtPrecoUnitario.clear();
        datePickerVenda.setValue(LocalDate.now());
        comboPagamento.setValue("Dinheiro");
        
        itensVenda.clear();
        valorTotalVenda = 0.0;
        lblValorTotal.setText("Valor Total: R$ 0,00");
    }

    // ==================== ATUALIZAR VALOR TOTAL ====================
    
    private void atualizarValorTotal() {
        valorTotalVenda = itensVenda.stream()
            .mapToDouble(ItemVenda::getSubtotal)
            .sum();
        
        lblValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotalVenda));
    }

    // ==================== BUSCAR VENDAS ====================
    
    @FXML
    private void onBuscar(javafx.scene.input.KeyEvent event) {
        String busca = txtBusca.getText().toLowerCase().trim();
        String statusFiltro = comboFiltroStatus.getValue();
        
        if (busca.isEmpty() && "Todos".equals(statusFiltro)) {
            vendasFiltradas.setAll(todasVendas);
        } else {
            List<Venda> filtradas = todasVendas.stream()
                .filter(v -> {
                    boolean matchBusca = busca.isEmpty() || 
                        (v.getNomeCliente() != null && v.getNomeCliente().toLowerCase().contains(busca)) ||
                        String.valueOf(v.getIdVenda()).contains(busca);
                    
                    boolean matchStatus = "Todos".equals(statusFiltro) || 
                        v.getStatus().equals(statusFiltro);
                    
                    return matchBusca && matchStatus;
                })
                .collect(Collectors.toList());
            
            vendasFiltradas.setAll(filtradas);
        }
    }

    // ==================== FILTRAR POR STATUS ====================
    
    @FXML
    private void onFiltrarStatus(ActionEvent event) {
        onBuscar(null);
    }

    // ==================== AÇÕES DA TABELA DE VENDAS ====================
    
    private void mostrarDetalhesVenda(Venda venda) {
        List<ItemVenda> itens = vendaDAO.buscarItensPorVenda(venda.getIdVenda());
        
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("ID da Venda: ").append(venda.getIdVenda()).append("\n");
        detalhes.append("Cliente: ").append(venda.getNomeCliente()).append("\n");
        detalhes.append("Data: ").append(venda.getDataFormatada()).append("\n");
        detalhes.append("Status: ").append(venda.getStatus()).append("\n");
        detalhes.append("Forma de Pagamento: ").append(venda.getObservacao()).append("\n\n");
        detalhes.append("ITENS DA VENDA:\n");
        detalhes.append("----------------------------------------\n");
        
        for (ItemVenda item : itens) {
            detalhes.append(String.format("• %s\n", item.getNomeProduto()));
            detalhes.append(String.format("  Qtd: %d | Preço Unit: R$ %.2f | Subtotal: R$ %.2f\n\n", 
                item.getQuantidade(), item.getPrecoUnitario(), item.getSubtotal()));
        }
        
        detalhes.append("----------------------------------------\n");
        detalhes.append(String.format("VALOR TOTAL: R$ %.2f", venda.getValorTotal()));
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalhes da Venda");
        alert.setHeaderText(null);
        alert.setContentText(detalhes.toString());
        alert.showAndWait();
    }
    
    private void concluirVenda(Venda venda) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Conclusão");
        confirmacao.setHeaderText("Deseja concluir esta venda?");
        confirmacao.setContentText("Venda #" + venda.getIdVenda() + " - " + venda.getNomeCliente());
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            venda.setStatus("Concluída");
            String msg = vendaDAO.atualizar(venda);
            
            if (msg.contains("sucesso")) {
                mostrarSucesso("Venda concluída com sucesso!");
                carregarDadosIniciais();
            } else {
                mostrarErro("Erro ao concluir venda", msg);
            }
        }
    }
    
    private void cancelarVenda(Venda venda) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Cancelamento");
        confirmacao.setHeaderText("Deseja cancelar esta venda?");
        confirmacao.setContentText("Venda #" + venda.getIdVenda() + " - " + venda.getNomeCliente());
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // O DAO cancela e devolve os itens ao estoque na mesma transação
            String msg = vendaDAO.cancelar(venda.getIdVenda());

            if (msg.contains("sucesso")) {
                mostrarSucesso("Venda cancelada e itens devolvidos ao estoque!");
                carregarDadosIniciais();
            } else {
                mostrarErro("Erro ao cancelar venda", msg);
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}