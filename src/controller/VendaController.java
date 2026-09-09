package controller;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Cliente;
import model.ItemVenda;
import model.Produto;
import model.Venda;

public class VendaController {

    // ============================================================
    // FORMULÁRIO
    // ============================================================

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Produto> comboProduto;

    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPrecoUnitario;

    @FXML private DatePicker datePickerVenda;

    @FXML private ComboBox<String> comboPagamento;


    // ============================================================
    // ITENS DA VENDA
    // ============================================================

    @FXML private TableView<ItemVenda> tableItens;

    @FXML private TableColumn<ItemVenda, String> colItemProduto;
    @FXML private TableColumn<ItemVenda, Integer> colItemQuantidade;
    @FXML private TableColumn<ItemVenda, String> colItemPreco;
    @FXML private TableColumn<ItemVenda, String> colItemSubtotal;
    @FXML private TableColumn<ItemVenda, Void> colItemAcoes;

    @FXML private Label lblValorTotal;


    // ============================================================
    // BUSCA E FILTROS
    // ============================================================

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> comboFiltroStatus;


    // ============================================================
    // HISTÓRICO DE VENDAS
    // ============================================================

    @FXML private TableView<Venda> tableVendas;

    @FXML private TableColumn<Venda, Integer> colId;
    @FXML private TableColumn<Venda, String> colCliente;
    @FXML private TableColumn<Venda, String> colData;
    @FXML private TableColumn<Venda, String> colValor;
    @FXML private TableColumn<Venda, String> colStatus;
    @FXML private TableColumn<Venda, Void> colAcoes;


    // ============================================================
    // LISTAS
    // ============================================================

    private final ObservableList<Cliente> todosClientes =
            FXCollections.observableArrayList();

    private final ObservableList<Produto> todosProdutos =
            FXCollections.observableArrayList();

    private final ObservableList<ItemVenda> itensVenda =
            FXCollections.observableArrayList();

    private final ObservableList<Venda> todasVendas =
            FXCollections.observableArrayList();

    private final ObservableList<Venda> vendasFiltradas =
            FXCollections.observableArrayList();


    // ============================================================
    // DAOs
    // ============================================================

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();


    // ============================================================
    // CONTROLE
    // ============================================================

    private double valorTotalVenda = 0.0;

    private String formaPagamentoSelecionada = "Dinheiro";

    private final NumberFormat moedaBrasil =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));


    // ============================================================
    // INITIALIZE
    // ============================================================

    @FXML
    public void initialize() {

        configurarComboCliente();
        configurarComboProduto();
        configurarComboPagamento();
        configurarComboFiltroStatus();

        configurarCampoQuantidade();
        configurarCampoPreco();

        configurarTabelaItens();
        configurarTabelaVendas();

        datePickerVenda.setValue(LocalDate.now());

        carregarDadosIniciais();

        atualizarValorTotal();
    }


    // ============================================================
    // CLIENTES
    // ============================================================

    private void configurarComboCliente() {

        comboCliente.setItems(todosClientes);

        comboCliente.setConverter(
                new javafx.util.StringConverter<Cliente>() {

                    @Override
                    public String toString(Cliente cliente) {

                        if (cliente == null) {
                            return "";
                        }

                        return cliente.getNome();
                    }

                    @Override
                    public Cliente fromString(String texto) {

                        if (texto == null || texto.isBlank()) {
                            return null;
                        }

                        return todosClientes.stream()
                                .filter(cliente ->
                                        cliente.getNome() != null
                                        && cliente.getNome()
                                                .equalsIgnoreCase(
                                                        texto.trim()
                                                )
                                )
                                .findFirst()
                                .orElse(null);
                    }
                }
        );

        comboCliente.getEditor()
                .textProperty()
                .addListener((obs, antigo, novo) -> {

                    if (comboCliente.isShowing()) {
                        return;
                    }

                    if (novo == null || novo.isBlank()) {

                        comboCliente.setItems(todosClientes);

                        return;
                    }

                    String busca = novo.toLowerCase().trim();

                    ObservableList<Cliente> filtrados =
                            todosClientes.stream()
                                    .filter(cliente ->
                                            cliente.getNome() != null
                                            && cliente.getNome()
                                                    .toLowerCase()
                                                    .contains(busca)
                                    )
                                    .collect(
                                            Collectors.toCollection(
                                                    FXCollections::observableArrayList
                                            )
                                    );

                    comboCliente.setItems(
                            filtrados.isEmpty()
                                    ? todosClientes
                                    : filtrados
                    );
                });
    }


    // ============================================================
    // PRODUTOS
    // ============================================================

    private void configurarComboProduto() {

        comboProduto.setItems(todosProdutos);

        comboProduto.setConverter(
                new javafx.util.StringConverter<Produto>() {

                    @Override
                    public String toString(Produto produto) {

                        if (produto == null) {
                            return "";
                        }

                        return produto.getNome()
                                + " (Cód: "
                                + produto.getCodigo()
                                + ")";
                    }

                    @Override
                    public Produto fromString(String texto) {

                        if (texto == null || texto.isBlank()) {
                            return null;
                        }

                        return todosProdutos.stream()
                                .filter(produto -> {

                                    String descricao =
                                            produto.getNome()
                                            + " (Cód: "
                                            + produto.getCodigo()
                                            + ")";

                                    return descricao.equalsIgnoreCase(
                                            texto.trim()
                                    );
                                })
                                .findFirst()
                                .orElse(null);
                    }
                }
        );

        comboProduto.getEditor()
                .textProperty()
                .addListener((obs, antigo, novo) -> {

                    if (comboProduto.isShowing()) {
                        return;
                    }

                    if (novo == null || novo.isBlank()) {

                        comboProduto.setItems(todosProdutos);

                        return;
                    }

                    String busca = novo.toLowerCase().trim();

                    ObservableList<Produto> filtrados =
                            todosProdutos.stream()
                                    .filter(produto -> {

                                        boolean nome =
                                                produto.getNome() != null
                                                && produto.getNome()
                                                        .toLowerCase()
                                                        .contains(busca);

                                        boolean codigo =
                                                String.valueOf(
                                                        produto.getCodigo()
                                                ).contains(busca);

                                        return nome || codigo;
                                    })
                                    .collect(
                                            Collectors.toCollection(
                                                    FXCollections::observableArrayList
                                            )
                                    );

                    comboProduto.setItems(
                            filtrados.isEmpty()
                                    ? todosProdutos
                                    : filtrados
                    );
                });

        comboProduto.valueProperty()
                .addListener((obs, antigo, produto) -> {

                    if (produto != null) {

                        txtPrecoUnitario.setText(
                                String.format(
                                        new Locale("pt", "BR"),
                                        "%.2f",
                                        produto.getPrecoVenda()
                                )
                        );
                    }
                });
    }


    // ============================================================
    // PAGAMENTO
    // ============================================================

    private void configurarComboPagamento() {

        comboPagamento.setItems(
                FXCollections.observableArrayList(
                        "Dinheiro",
                        "Cartão de Crédito",
                        "Cartão de Débito",
                        "Pix",
                        "Transferência"
                )
        );

        comboPagamento.setValue("Dinheiro");

        comboPagamento.setOnAction(e -> {

            if (comboPagamento.getValue() != null) {

                formaPagamentoSelecionada =
                        comboPagamento.getValue();
            }
        });
    }


    // ============================================================
    // FILTRO STATUS
    // ============================================================

    private void configurarComboFiltroStatus() {

        comboFiltroStatus.setItems(
                FXCollections.observableArrayList(
                        "Todos",
                        "Pendente",
                        "Concluída",
                        "Cancelada"
                )
        );

        comboFiltroStatus.setValue("Todos");
    }


    // ============================================================
    // VALIDAÇÃO QUANTIDADE
    // ============================================================

    private void configurarCampoQuantidade() {

        txtQuantidade.textProperty()
                .addListener((obs, antigo, novo) -> {

                    if (novo == null) {
                        return;
                    }

                    if (!novo.matches("\\d*")) {

                        txtQuantidade.setText(
                                novo.replaceAll("[^\\d]", "")
                        );
                    }
                });
    }


    // ============================================================
    // VALIDAÇÃO PREÇO
    // ============================================================

    private void configurarCampoPreco() {

        txtPrecoUnitario.textProperty()
                .addListener((obs, antigo, novo) -> {

                    if (novo == null || novo.isEmpty()) {
                        return;
                    }

                    if (!novo.matches("\\d*[.,]?\\d{0,2}")) {

                        txtPrecoUnitario.setText(antigo);
                    }
                });
    }


    // ============================================================
    // TABELA DE ITENS
    // ============================================================

    private void configurarTabelaItens() {

        colItemProduto.setCellValueFactory(
                new PropertyValueFactory<>("nomeProduto")
        );

        colItemQuantidade.setCellValueFactory(
                new PropertyValueFactory<>("quantidade")
        );

        colItemPreco.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                moedaBrasil.format(
                                        cellData
                                                .getValue()
                                                .getPrecoUnitario()
                                )
                        )
        );

        colItemSubtotal.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                moedaBrasil.format(
                                        cellData
                                                .getValue()
                                                .getSubtotal()
                                )
                        )
        );

        colItemAcoes.setCellFactory(col ->
                new TableCell<ItemVenda, Void>() {

                    private final Button btnRemover =
                            new Button("🗑");

                    {
                        btnRemover
                                .getStyleClass()
                                .add("table-action-delete");

                        btnRemover.setTooltip(
                                new Tooltip("Remover item")
                        );

                        btnRemover.setOnAction(e -> {

                            ItemVenda item =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            itensVenda.remove(item);

                            atualizarValorTotal();
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        setGraphic(
                                empty
                                        ? null
                                        : btnRemover
                        );
                    }
                }
        );

        tableItens.setItems(itensVenda);

        tableItens.setPlaceholder(
                new Label(
                        "Nenhum produto adicionado à venda."
                )
        );
    }


    // ============================================================
    // TABELA DO HISTÓRICO
    // ============================================================

    private void configurarTabelaVendas() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("idVenda")
        );

        colCliente.setCellValueFactory(
                new PropertyValueFactory<>("nomeCliente")
        );

        colData.setCellValueFactory(
                new PropertyValueFactory<>("dataFormatada")
        );

        colValor.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                moedaBrasil.format(
                                        cellData
                                                .getValue()
                                                .getValorTotal()
                                )
                        )
        );

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );


        // STATUS COLORIDO

        colStatus.setCellFactory(col ->
                new TableCell<Venda, String>() {

                    @Override
                    protected void updateItem(
                            String status,
                            boolean empty) {

                        super.updateItem(status, empty);

                        setGraphic(null);
                        setText(null);

                        if (empty
                                || status == null
                                || status.isBlank()) {
                            return;
                        }

                        Label badge =
                                new Label(status);

                        badge.getStyleClass()
                                .add("sale-status-badge");

                        switch (status) {

                            case "Concluída":
                                badge.getStyleClass()
                                        .add(
                                                "sale-status-completed"
                                        );
                                break;

                            case "Cancelada":
                                badge.getStyleClass()
                                        .add(
                                                "sale-status-cancelled"
                                        );
                                break;

                            default:
                                badge.getStyleClass()
                                        .add(
                                                "sale-status-pending"
                                        );
                                break;
                        }

                        setGraphic(badge);
                    }
                }
        );


        // AÇÕES

        colAcoes.setCellFactory(col ->
                new TableCell<Venda, Void>() {

                    private final Button btnDetalhes =
                            new Button("Ver");

                    private final Button btnConcluir =
                            new Button("Concluir");

                    private final Button btnCancelar =
                            new Button("Cancelar");

                    private final HBox boxCompleto =
                            new HBox(
                                    6,
                                    btnDetalhes,
                                    btnConcluir,
                                    btnCancelar
                            );

                    private final HBox boxDetalhes =
                            new HBox(
                                    6,
                                    btnDetalhes
                            );

                    {
                        btnDetalhes
                                .getStyleClass()
                                .add("sale-action-view");

                        btnConcluir
                                .getStyleClass()
                                .add("sale-action-complete");

                        btnCancelar
                                .getStyleClass()
                                .add("sale-action-cancel");


                        btnDetalhes.setTooltip(
                                new Tooltip(
                                        "Ver detalhes da venda"
                                )
                        );

                        btnConcluir.setTooltip(
                                new Tooltip(
                                        "Concluir venda"
                                )
                        );

                        btnCancelar.setTooltip(
                                new Tooltip(
                                        "Cancelar venda"
                                )
                        );


                        btnDetalhes.setOnAction(e -> {

                            Venda venda =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            mostrarDetalhesVenda(venda);
                        });


                        btnConcluir.setOnAction(e -> {

                            Venda venda =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            concluirVenda(venda);
                        });


                        btnCancelar.setOnAction(e -> {

                            Venda venda =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            cancelarVenda(venda);
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {

                            setGraphic(null);

                            return;
                        }

                        Venda venda =
                                getTableView()
                                    .getItems()
                                    .get(getIndex());

                        if ("Concluída".equals(
                                    venda.getStatus())
                                || "Cancelada".equals(
                                    venda.getStatus())) {

                            setGraphic(boxDetalhes);

                        } else {

                            setGraphic(boxCompleto);
                        }
                    }
                }
        );

        tableVendas.setItems(vendasFiltradas);

        tableVendas.setPlaceholder(
                new Label(
                        "Nenhuma venda encontrada."
                )
        );
    }


    // ============================================================
    // CARREGAMENTO
    // ============================================================

    private void carregarDadosIniciais() {

        try {

            List<Cliente> clientes =
                    clienteDAO.getAll();

            List<Produto> produtos =
                    produtoDAO.getAll();

            List<Venda> vendas =
                    vendaDAO.getAll();

            todosClientes.setAll(
                    clientes != null
                            ? clientes
                            : new ArrayList<>()
            );

            todosProdutos.setAll(
                    produtos != null
                            ? produtos
                            : new ArrayList<>()
            );

            todasVendas.setAll(
                    vendas != null
                            ? vendas
                            : new ArrayList<>()
            );

            vendasFiltradas.setAll(todasVendas);

            comboCliente.setItems(todosClientes);
            comboProduto.setItems(todosProdutos);

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao carregar dados",
                    "Não foi possível carregar os dados da tela de vendas."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // ADICIONAR ITEM
    // ============================================================

    @FXML
    private void onAdicionarItem(ActionEvent event) {

        Cliente clienteSelecionado =
                comboCliente.getValue();

        Produto produtoSelecionado =
                comboProduto.getValue();


        if (clienteSelecionado == null) {

            mostrarAviso(
                    "Cliente não selecionado",
                    "Selecione um cliente válido antes de adicionar produtos."
            );

            comboCliente.requestFocus();

            return;
        }


        if (produtoSelecionado == null) {

            mostrarAviso(
                    "Produto não selecionado",
                    "Selecione um produto válido."
            );

            comboProduto.requestFocus();

            return;
        }


        if (produtoSelecionado.getQtdeEstoque() <= 0) {

            mostrarAviso(
                    "Produto sem estoque",
                    "Este produto não possui unidades disponíveis."
            );

            return;
        }


        int quantidade;

        try {

            quantidade =
                    Integer.parseInt(
                            txtQuantidade
                                    .getText()
                                    .trim()
                    );

            if (quantidade <= 0) {

                mostrarAviso(
                        "Quantidade inválida",
                        "A quantidade deve ser maior que zero."
                );

                txtQuantidade.requestFocus();

                return;
            }

        } catch (Exception e) {

            mostrarAviso(
                    "Quantidade inválida",
                    "Informe uma quantidade válida."
            );

            txtQuantidade.requestFocus();

            return;
        }


        if (quantidade
                > produtoSelecionado.getQtdeEstoque()) {

            mostrarAviso(
                    "Estoque insuficiente",
                    "Existem apenas "
                            + produtoSelecionado
                                    .getQtdeEstoque()
                            + " unidade(s) disponíveis."
            );

            return;
        }


        double precoUnitario;

        try {

            String precoTexto =
                    txtPrecoUnitario
                            .getText()
                            .trim()
                            .replace(",", ".");

            precoUnitario =
                    Double.parseDouble(precoTexto);

            if (precoUnitario <= 0) {

                mostrarAviso(
                        "Preço inválido",
                        "O preço deve ser maior que zero."
                );

                txtPrecoUnitario.requestFocus();

                return;
            }

        } catch (Exception e) {

            mostrarAviso(
                    "Preço inválido",
                    "Informe um preço válido."
            );

            txtPrecoUnitario.requestFocus();

            return;
        }


        Optional<ItemVenda> existente =
                itensVenda.stream()
                        .filter(item ->
                                item.getIdProduto()
                                        == produtoSelecionado.getId()
                        )
                        .findFirst();


        if (existente.isPresent()) {

            ItemVenda item =
                    existente.get();

            int novaQuantidade =
                    item.getQuantidade()
                    + quantidade;


            if (novaQuantidade
                    > produtoSelecionado
                            .getQtdeEstoque()) {

                mostrarAviso(
                        "Estoque insuficiente",
                        "A quantidade total informada ultrapassa o estoque disponível."
                );

                return;
            }


            item.setQuantidade(
                    novaQuantidade
            );

            item.setPrecoUnitario(
                    precoUnitario
            );

            item.calcularSubtotal();

            tableItens.refresh();

        } else {

            ItemVenda item =
                    new ItemVenda();

            item.setIdProduto(
                    produtoSelecionado.getId()
            );

            item.setNomeProduto(
                    produtoSelecionado.getNome()
            );

            item.setQuantidade(
                    quantidade
            );

            item.setPrecoUnitario(
                    precoUnitario
            );

            item.calcularSubtotal();

            itensVenda.add(item);
        }


        atualizarValorTotal();

        limparCamposProduto();

        comboProduto.requestFocus();
    }


    // ============================================================
    // FINALIZAR VENDA
    // ============================================================

    @FXML
    private void onFinalizarVenda(
            ActionEvent event) {

        Cliente cliente =
                comboCliente.getValue();


        if (cliente == null) {

            mostrarAviso(
                    "Cliente não selecionado",
                    "Selecione um cliente antes de finalizar a venda."
            );

            comboCliente.requestFocus();

            return;
        }


        if (itensVenda.isEmpty()) {

            mostrarAviso(
                    "Venda sem produtos",
                    "Adicione pelo menos um produto à venda."
            );

            comboProduto.requestFocus();

            return;
        }


        LocalDate data =
                datePickerVenda.getValue();


        if (data == null) {

            mostrarAviso(
                    "Data não informada",
                    "Informe a data da venda."
            );

            datePickerVenda.requestFocus();

            return;
        }


        if (comboPagamento.getValue() == null) {

            mostrarAviso(
                    "Forma de pagamento",
                    "Selecione uma forma de pagamento."
            );

            comboPagamento.requestFocus();

            return;
        }


        formaPagamentoSelecionada =
                comboPagamento.getValue();


        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacao.setTitle(
                "Finalizar venda"
        );

        confirmacao.setHeaderText(
                "Confirmar registro da venda?"
        );

        confirmacao.setContentText(
                "Cliente: "
                        + cliente.getNome()
                        + "\nItens: "
                        + itensVenda.size()
                        + "\nTotal: "
                        + moedaBrasil.format(
                                valorTotalVenda
                        )
        );


        Optional<ButtonType> resposta =
                confirmacao.showAndWait();


        if (resposta.isEmpty()
                || resposta.get()
                != ButtonType.OK) {

            return;
        }


        Venda venda =
                new Venda();

        venda.setIdCliente(
                cliente.getId()
        );

        venda.setData(
                LocalDateTime.of(
                        data,
                        LocalTime.now()
                )
        );

        venda.setValorTotal(
                valorTotalVenda
        );

        venda.setStatus(
                "Pendente"
        );

        venda.setObservacao(
                formaPagamentoSelecionada
        );


        List<ItemVenda> itens =
                new ArrayList<>(
                        itensVenda
                );


        try {

            String resultado =
                    vendaDAO.inserir(
                            venda,
                            itens
                    );


            if (resultado.contains(
                    "sucesso")) {

                mostrarSucesso(
                        "Venda registrada com sucesso!\n"
                        + "Venda #"
                        + venda.getIdVenda()
                );

                limparFormulario();

                carregarDadosIniciais();

                aplicarFiltroVendas();

            } else {

                mostrarErro(
                        "Erro ao registrar venda",
                        resultado
                );
            }

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao registrar venda",
                    "Não foi possível salvar a venda."
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // LIMPAR
    // ============================================================

    @FXML
    private void onLimpar(
            ActionEvent event) {

        if (!itensVenda.isEmpty()) {

            Alert confirmacao =
                    new Alert(
                            Alert.AlertType.CONFIRMATION
                    );

            confirmacao.setTitle(
                    "Limpar venda"
            );

            confirmacao.setHeaderText(
                    "Descartar a venda atual?"
            );

            confirmacao.setContentText(
                    "Os produtos adicionados serão removidos da venda atual."
            );


            Optional<ButtonType> resultado =
                    confirmacao.showAndWait();


            if (resultado.isEmpty()
                    || resultado.get()
                    != ButtonType.OK) {

                return;
            }
        }

        limparFormulario();
    }


    private void limparFormulario() {

        comboCliente.setValue(null);
        comboCliente.getEditor().clear();
        comboCliente.setItems(todosClientes);

        limparCamposProduto();

        datePickerVenda.setValue(
                LocalDate.now()
        );

        comboPagamento.setValue(
                "Dinheiro"
        );

        formaPagamentoSelecionada =
                "Dinheiro";

        itensVenda.clear();

        valorTotalVenda = 0.0;

        atualizarValorTotal();

        comboCliente.requestFocus();
    }


    private void limparCamposProduto() {

        comboProduto.setValue(null);
        comboProduto.getEditor().clear();
        comboProduto.setItems(todosProdutos);

        txtQuantidade.clear();
        txtPrecoUnitario.clear();
    }


    // ============================================================
    // TOTAL
    // ============================================================

    private void atualizarValorTotal() {

        valorTotalVenda =
                itensVenda.stream()
                        .mapToDouble(
                                ItemVenda::getSubtotal
                        )
                        .sum();

        lblValorTotal.setText(
                moedaBrasil.format(
                        valorTotalVenda
                )
        );
    }


    // ============================================================
    // BUSCA
    // ============================================================

    @FXML
    private void onBuscar(
            javafx.scene.input.KeyEvent event) {

        aplicarFiltroVendas();
    }


    @FXML
    private void onFiltrarStatus(
            ActionEvent event) {

        aplicarFiltroVendas();
    }


    private void aplicarFiltroVendas() {

        String busca =
                txtBusca.getText() == null
                        ? ""
                        : txtBusca
                                .getText()
                                .toLowerCase()
                                .trim();

        String status =
                comboFiltroStatus.getValue();


        List<Venda> filtradas =
                todasVendas.stream()
                        .filter(venda -> {

                            boolean matchBusca =
                                    busca.isEmpty()
                                    || (
                                        venda.getNomeCliente()
                                                != null
                                        && venda
                                                .getNomeCliente()
                                                .toLowerCase()
                                                .contains(busca)
                                    )
                                    || String.valueOf(
                                            venda.getIdVenda()
                                    ).contains(busca);


                            boolean matchStatus =
                                    status == null
                                    || "Todos".equals(status)
                                    || status.equals(
                                            venda.getStatus()
                                    );


                            return matchBusca
                                    && matchStatus;
                        })
                        .collect(
                                Collectors.toList()
                        );


        vendasFiltradas.setAll(
                filtradas
        );
    }


    // ============================================================
    // DETALHES
    // ============================================================

    private void mostrarDetalhesVenda(
            Venda venda) {

        List<ItemVenda> itens =
                vendaDAO.buscarItensPorVenda(
                        venda.getIdVenda()
                );


        StringBuilder detalhes =
                new StringBuilder();


        detalhes.append(
                "Venda #"
        ).append(
                venda.getIdVenda()
        ).append(
                "\n\n"
        );


        detalhes.append(
                "Cliente: "
        ).append(
                venda.getNomeCliente()
        ).append(
                "\n"
        );


        detalhes.append(
                "Data: "
        ).append(
                venda.getDataFormatada()
        ).append(
                "\n"
        );


        detalhes.append(
                "Status: "
        ).append(
                venda.getStatus()
        ).append(
                "\n"
        );


        detalhes.append(
                "Pagamento: "
        ).append(
                venda.getObservacao()
        ).append(
                "\n\n"
        );


        detalhes.append(
                "ITENS DA VENDA\n"
        );

        detalhes.append(
                "----------------------------------\n"
        );


        for (ItemVenda item : itens) {

            detalhes.append(
                    item.getNomeProduto()
            ).append(
                    "\n"
            );

            detalhes.append(
                    "Quantidade: "
            ).append(
                    item.getQuantidade()
            ).append(
                    "\n"
            );

            detalhes.append(
                    "Preço unitário: "
            ).append(
                    moedaBrasil.format(
                            item.getPrecoUnitario()
                    )
            ).append(
                    "\n"
            );

            detalhes.append(
                    "Subtotal: "
            ).append(
                    moedaBrasil.format(
                            item.getSubtotal()
                    )
            ).append(
                    "\n\n"
            );
        }


        detalhes.append(
                "----------------------------------\n"
        );

        detalhes.append(
                "TOTAL: "
        ).append(
                moedaBrasil.format(
                        venda.getValorTotal()
                )
        );


        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Detalhes da Venda"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                detalhes.toString()
        );

        alert.showAndWait();
    }


    // ============================================================
    // CONCLUIR
    // ============================================================

    private void concluirVenda(
            Venda venda) {

        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacao.setTitle(
                "Concluir venda"
        );

        confirmacao.setHeaderText(
                "Confirmar conclusão da venda?"
        );

        confirmacao.setContentText(
                "Venda #"
                        + venda.getIdVenda()
                        + "\nCliente: "
                        + venda.getNomeCliente()
        );


        Optional<ButtonType> resultado =
                confirmacao.showAndWait();


        if (resultado.isPresent()
                && resultado.get()
                == ButtonType.OK) {

            venda.setStatus(
                    "Concluída"
            );


            String mensagem =
                    vendaDAO.atualizar(
                            venda
                    );


            if (mensagem.contains(
                    "sucesso")) {

                mostrarSucesso(
                        "Venda concluída com sucesso!"
                );

                carregarDadosIniciais();

                aplicarFiltroVendas();

            } else {

                mostrarErro(
                        "Erro ao concluir venda",
                        mensagem
                );
            }
        }
    }


    // ============================================================
    // CANCELAR
    // ============================================================

    private void cancelarVenda(
            Venda venda) {

        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacao.setTitle(
                "Cancelar venda"
        );

        confirmacao.setHeaderText(
                "Confirmar cancelamento da venda?"
        );

        confirmacao.setContentText(
                "Venda #"
                        + venda.getIdVenda()
                        + "\nCliente: "
                        + venda.getNomeCliente()
                        + "\n\nOs itens serão devolvidos ao estoque."
        );


        Optional<ButtonType> resultado =
                confirmacao.showAndWait();


        if (resultado.isPresent()
                && resultado.get()
                == ButtonType.OK) {

            String mensagem =
                    vendaDAO.cancelar(
                            venda.getIdVenda()
                    );


            if (mensagem.contains(
                    "sucesso")) {

                mostrarSucesso(
                        "Venda cancelada.\n"
                        + "Os produtos foram devolvidos ao estoque."
                );

                carregarDadosIniciais();

                aplicarFiltroVendas();

            } else {

                mostrarErro(
                        "Erro ao cancelar venda",
                        mensagem
                );
            }
        }
    }


    // ============================================================
    // ALERTAS
    // ============================================================

    private void mostrarSucesso(
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Sucesso"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }


    private void mostrarAviso(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                titulo
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }


    private void mostrarErro(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Erro"
        );

        alert.setHeaderText(
                titulo
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }
}