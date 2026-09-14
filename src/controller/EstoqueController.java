package controller;

import database.LoteProdutoDAO;
import database.ProdutoDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import model.LoteProduto;
import model.Produto;

import util.AlertaUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class EstoqueController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cbFiltroStatus;

    @FXML
    private Button btnAtualizar;

    @FXML
    private TableView<Produto> productTable;

    @FXML
    private TableColumn<Produto, String> colNomeProduto;

    @FXML
    private TableColumn<Produto, Integer> colCodigo;

    @FXML
    private TableColumn<Produto, Integer> colQuantidade;

    @FXML
    private TableColumn<Produto, Double> colPrecoVenda;

    @FXML
    private TableColumn<Produto, Void> colAcoes;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTotalItens;

    @FXML
    private Label lblValorTotal;


    private final ObservableList<Produto> data =
            FXCollections.observableArrayList();

    private final ObservableList<Produto> dataOriginal =
            FXCollections.observableArrayList();


    private final ProdutoDAO produtoDAO =
            new ProdutoDAO();

    private final LoteProdutoDAO loteProdutoDAO =
            new LoteProdutoDAO();


    private final NumberFormat moedaBrasil =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );


    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );


    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @FXML
    private void initialize() {

        configurarColunas();

        configurarColunaQuantidade();

        configurarColunaPrecoVenda();

        addActionsColumn();


        productTable.setItems(
                data
        );


        productTable.setPlaceholder(
                new Label(
                        "Nenhum produto encontrado."
                )
        );


        cbFiltroStatus.getItems().addAll(
                "Todos",
                "Em estoque",
                "Estoque baixo",
                "Fora de estoque"
        );


        cbFiltroStatus
                .getSelectionModel()
                .select(
                        "Todos"
                );


        txtBuscar
                .textProperty()
                .addListener(
                        (obs, antigo, novo) ->
                                aplicarFiltro()
                );


        cbFiltroStatus.setOnAction(
                e -> aplicarFiltro()
        );


        btnAtualizar.setOnAction(
                e -> carregarProdutos()
        );


        carregarProdutos();
    }


    // ============================================================
    // CONFIGURAÇÃO DAS COLUNAS
    // ============================================================

    private void configurarColunas() {

        colNomeProduto.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nome"
                )
        );


        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "codigo"
                )
        );


        colQuantidade.setCellValueFactory(
                new PropertyValueFactory<>(
                        "qtdeEstoque"
                )
        );


        colPrecoVenda.setCellValueFactory(
                new PropertyValueFactory<>(
                        "precoVenda"
                )
        );
    }


    // ============================================================
    // PREÇO DE VENDA
    // ============================================================

    private void configurarColunaPrecoVenda() {

        colPrecoVenda.setCellFactory(
                col -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Double value,
                            boolean empty) {

                        super.updateItem(
                                value,
                                empty
                        );


                        if (empty
                                || value == null) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    moedaBrasil.format(
                                            value
                                    )
                            );
                        }
                    }
                }
        );
    }


    // ============================================================
    // QUANTIDADE
    // ============================================================

    private void configurarColunaQuantidade() {

        colQuantidade.setCellFactory(
                col -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Integer quantidade,
                            boolean empty) {

                        super.updateItem(
                                quantidade,
                                empty
                        );


                        setGraphic(
                                null
                        );

                        setText(
                                null
                        );


                        if (empty
                                || quantidade == null) {

                            return;
                        }


                        Label badge =
                                new Label();


                        badge
                                .getStyleClass()
                                .add(
                                        "stock-badge"
                                );


                        if (quantidade == 0) {

                            badge.setText(
                                    "0 - Sem estoque"
                            );


                            badge
                                    .getStyleClass()
                                    .add(
                                            "stock-badge-empty"
                                    );

                        } else if (quantidade <= 10) {

                            badge.setText(
                                    String.valueOf(
                                            quantidade
                                    )
                            );


                            badge
                                    .getStyleClass()
                                    .add(
                                            "stock-badge-low"
                                    );

                        } else {

                            badge.setText(
                                    String.valueOf(
                                            quantidade
                                    )
                            );


                            badge
                                    .getStyleClass()
                                    .add(
                                            "stock-badge-ok"
                                    );
                        }


                        setGraphic(
                                badge
                        );
                    }
                }
        );
    }


    // ============================================================
    // CARREGAR PRODUTOS
    // ============================================================

    private void carregarProdutos() {

        try {

            List<Produto> produtos =
                    produtoDAO.getAll();


            if (produtos != null) {

                dataOriginal.setAll(
                        produtos
                );

            } else {

                dataOriginal.clear();
            }


            aplicarFiltro();


            lblStatus.setText(
                    "Produtos carregados: "
                            + dataOriginal.size()
            );


        } catch (Exception e) {

            lblStatus.setText(
                    "Erro ao carregar produtos"
            );


            AlertaUtil.erro(
                    "Erro",
                    "Erro ao carregar produtos do banco: "
                            + e.getMessage()
            );


            e.printStackTrace();
        }
    }


    // ============================================================
    // FILTROS
    // ============================================================

    private void aplicarFiltro() {

        String busca =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar
                                .getText()
                                .trim()
                                .toLowerCase();


        String statusFiltro =
                cbFiltroStatus.getValue();


        ObservableList<Produto> filtrados =
                FXCollections.observableArrayList();


        for (Produto produto : dataOriginal) {

            boolean correspondeBusca =
                    busca.isEmpty()

                            || (
                            produto.getNome() != null

                                    && produto
                                    .getNome()
                                    .toLowerCase()
                                    .contains(
                                            busca
                                    )
                    )

                            || String
                            .valueOf(
                                    produto.getCodigo()
                            )
                            .contains(
                                    busca
                            );


            boolean correspondeStatus =
                    true;


            if (statusFiltro != null
                    && !statusFiltro.equals(
                            "Todos"
                    )) {

                int quantidade =
                        produto.getQtdeEstoque();


                switch (statusFiltro) {

                    case "Em estoque":

                        correspondeStatus =
                                quantidade > 10;

                        break;


                    case "Estoque baixo":

                        correspondeStatus =
                                quantidade > 0
                                        && quantidade <= 10;

                        break;


                    case "Fora de estoque":

                        correspondeStatus =
                                quantidade == 0;

                        break;


                    default:

                        correspondeStatus =
                                true;

                        break;
                }
            }


            if (correspondeBusca
                    && correspondeStatus) {

                filtrados.add(
                        produto
                );
            }
        }


        data.setAll(
                filtrados
        );


        atualizarResumo(
                data
        );
    }


    // ============================================================
    // COLUNA DE AÇÕES
    // ============================================================

    private void addActionsColumn() {

        Callback<
                TableColumn<Produto, Void>,
                TableCell<Produto, Void>
                > cellFactory = param ->

                new TableCell<>() {


                    private final Button btnEntrada =
                            new Button(
                                    "➕"
                            );


                    private final Button btnLotes =
                            new Button(
                                    "📦"
                            );


                    private final Button btnEditar =
                            new Button(
                                    "✏"
                            );


                    private final Button btnExcluir =
                            new Button(
                                    "🗑"
                            );


                    private final HBox box =
                            new HBox(
                                    6,
                                    btnEntrada,
                                    btnLotes,
                                    btnEditar,
                                    btnExcluir
                            );


                    {

                        // ====================================================
                        // ENTRADA DE ESTOQUE
                        // ====================================================

                        btnEntrada
                                .getStyleClass()
                                .add(
                                        "table-action-edit"
                                );


                        btnEntrada.setTooltip(
                                new Tooltip(
                                        "Registrar entrada de estoque"
                                )
                        );


                        btnEntrada.setOnAction(
                                e -> {

                                    Produto produto =
                                            getProdutoDaLinha();


                                    if (produto != null) {

                                        registrarEntradaEstoque(
                                                produto
                                        );
                                    }
                                }
                        );


                        // ====================================================
                        // VISUALIZAR LOTES
                        // ====================================================

                        btnLotes
                                .getStyleClass()
                                .add(
                                        "table-action-edit"
                                );


                        btnLotes.setTooltip(
                                new Tooltip(
                                        "Visualizar lotes"
                                )
                        );


                        btnLotes.setOnAction(
                                e -> {

                                    Produto produto =
                                            getProdutoDaLinha();


                                    if (produto != null) {

                                        visualizarLotes(
                                                produto
                                        );
                                    }
                                }
                        );


                        // ====================================================
                        // EDITAR PRODUTO
                        // ====================================================

                        btnEditar
                                .getStyleClass()
                                .add(
                                        "table-action-edit"
                                );


                        btnEditar.setTooltip(
                                new Tooltip(
                                        "Editar produto"
                                )
                        );


                        btnEditar.setOnAction(
                                e -> {

                                    Produto produto =
                                            getProdutoDaLinha();


                                    if (produto != null) {

                                        editarProduto(
                                                produto
                                        );
                                    }
                                }
                        );


                        // ====================================================
                        // EXCLUIR PRODUTO
                        // ====================================================

                        btnExcluir
                                .getStyleClass()
                                .add(
                                        "table-action-delete"
                                );


                        btnExcluir.setTooltip(
                                new Tooltip(
                                        "Excluir produto"
                                )
                        );


                        btnExcluir.setOnAction(
                                e -> {

                                    Produto produto =
                                            getProdutoDaLinha();


                                    if (produto != null) {

                                        excluirProduto(
                                                produto
                                        );
                                    }
                                }
                        );
                    }


                    private Produto getProdutoDaLinha() {

                        int indice =
                                getIndex();


                        if (indice < 0
                                || indice >= getTableView()
                                        .getItems()
                                        .size()) {

                            return null;
                        }


                        return getTableView()
                                .getItems()
                                .get(
                                        indice
                                );
                    }


                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );


                        setGraphic(
                                empty
                                        ? null
                                        : box
                        );
                    }
                };


        colAcoes.setCellFactory(
                cellFactory
        );
    }


    // ============================================================
    // REGISTRAR ENTRADA DE ESTOQUE
    // ============================================================

    private void registrarEntradaEstoque(
            Produto produto) {


        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Entrada de estoque"
        );


        dialog.setHeaderText(
                produto.getNome()
                        + " - Estoque atual: "
                        + produto.getQtdeEstoque()
        );


        TextField txtLote =
                new TextField();


        txtLote.setPromptText(
                "Ex.: L002"
        );


        TextField txtQuantidade =
                new TextField();


        txtQuantidade.setPromptText(
                "Ex.: 50"
        );


        TextField txtCusto =
                new TextField();


        txtCusto.setPromptText(
                "Ex.: 4,50"
        );


        // ============================================================
        // SOMENTE NÚMEROS NA QUANTIDADE
        // ============================================================

        txtQuantidade
                .textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            if (!novo.matches(
                                    "\\d*"
                            )) {

                                txtQuantidade.setText(
                                        novo.replaceAll(
                                                "[^\\d]",
                                                ""
                                        )
                                );
                            }
                        }
                );


        // ============================================================
        // VALIDAÇÃO DO CUSTO
        // ============================================================

        txtCusto
                .textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            if (!novo.matches(
                                    "\\d*([,.]\\d*)?"
                            )) {

                                txtCusto.setText(
                                        antigo
                                );
                            }
                        }
                );


        GridPane grid =
                new GridPane();


        grid.setHgap(
                12
        );


        grid.setVgap(
                12
        );


        grid.setPadding(
                new Insets(
                        20,
                        20,
                        10,
                        20
                )
        );


        grid.add(
                new Label(
                        "Produto:"
                ),
                0,
                0
        );


        grid.add(
                new Label(
                        produto.getNome()
                ),
                1,
                0
        );


        grid.add(
                new Label(
                        "Estoque atual:"
                ),
                0,
                1
        );


        grid.add(
                new Label(
                        String.valueOf(
                                produto.getQtdeEstoque()
                        )
                ),
                1,
                1
        );


        grid.add(
                new Label(
                        "Novo lote:"
                ),
                0,
                2
        );


        grid.add(
                txtLote,
                1,
                2
        );


        grid.add(
                new Label(
                        "Quantidade recebida:"
                ),
                0,
                3
        );


        grid.add(
                txtQuantidade,
                1,
                3
        );


        grid.add(
                new Label(
                        "Custo unitário:"
                ),
                0,
                4
        );


        grid.add(
                txtCusto,
                1,
                4
        );


        dialog
                .getDialogPane()
                .setContent(
                        grid
                );


        ButtonType btnConfirmar =
                new ButtonType(
                        "Registrar entrada",
                        ButtonBar.ButtonData.OK_DONE
                );


        ButtonType btnCancelar =
                new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );


        dialog
                .getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnConfirmar,
                        btnCancelar
                );


        Button confirmarButton =
                (Button) dialog
                        .getDialogPane()
                        .lookupButton(
                                btnConfirmar
                        );


        confirmarButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {


                    if (txtLote
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o lote."
                        );


                        event.consume();

                        return;
                    }


                    if (txtQuantidade
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe a quantidade recebida."
                        );


                        event.consume();

                        return;
                    }


                    if (txtCusto
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o custo unitário."
                        );


                        event.consume();

                        return;
                    }


                    try {

                        int quantidade =
                                Integer.parseInt(
                                        txtQuantidade
                                                .getText()
                                                .trim()
                                );


                        BigDecimal custo =
                                new BigDecimal(
                                        txtCusto
                                                .getText()
                                                .trim()
                                                .replace(
                                                        ",",
                                                        "."
                                                )
                                );


                        if (quantidade <= 0) {

                            AlertaUtil.erro(
                                    "Quantidade inválida",
                                    "A quantidade deve ser maior que zero."
                            );


                            event.consume();

                            return;
                        }


                        if (custo.compareTo(
                                BigDecimal.ZERO
                        ) < 0) {

                            AlertaUtil.erro(
                                    "Custo inválido",
                                    "O custo não pode ser negativo."
                            );


                            event.consume();
                        }


                    } catch (NumberFormatException e) {

                        AlertaUtil.erro(
                                "Valor inválido",
                                "Verifique a quantidade e o custo informados."
                        );


                        event.consume();
                    }
                }
        );


        Optional<ButtonType> resultado =
                dialog.showAndWait();


        if (resultado.isPresent()
                && resultado.get()
                == btnConfirmar) {


            try {

                int quantidade =
                        Integer.parseInt(
                                txtQuantidade
                                        .getText()
                                        .trim()
                        );


                BigDecimal custo =
                        new BigDecimal(
                                txtCusto
                                        .getText()
                                        .trim()
                                        .replace(
                                                ",",
                                                "."
                                        )
                        );


                LoteProduto lote =
                        new LoteProduto();


                lote.setIdProduto(
                        produto.getId()
                );


                lote.setCodigoLote(
                        txtLote
                                .getText()
                                .trim()
                );


                lote.setQuantidade(
                        quantidade
                );


                lote.setCustoUnitario(
                        custo
                );


                boolean sucesso =
                        loteProdutoDAO
                                .registrarEntrada(
                                        lote
                                );


                if (sucesso) {

                    carregarProdutos();


                    lblStatus.setText(
                            "Entrada de estoque registrada"
                    );


                    AlertaUtil.sucesso(
                            "Entrada registrada",
                            quantidade
                                    + " unidade(s) de "
                                    + produto.getNome()
                                    + " adicionadas ao estoque."
                    );


                } else {

                    AlertaUtil.erro(
                            "Erro",
                            "Não foi possível registrar a entrada de estoque."
                    );
                }


            } catch (Exception e) {

                AlertaUtil.erro(
                        "Erro",
                        "Erro ao registrar entrada.\n\n"
                                + e.getMessage()
                );


                e.printStackTrace();
            }
        }
    }


    // ============================================================
    // VISUALIZAR LOTES
    // ============================================================

    private void visualizarLotes(
            Produto produto) {


        List<LoteProduto> lotes =
                loteProdutoDAO
                        .buscarPorProduto(
                                produto.getId()
                        );


        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Lotes do produto"
        );


        dialog.setHeaderText(
                produto.getNome()
                        + " - Código "
                        + produto.getCodigo()
        );


        TableView<LoteProduto> tabela =
                new TableView<>();


        tabela.setPrefWidth(
                650
        );


        tabela.setPrefHeight(
                320
        );


        tabela.setPlaceholder(
                new Label(
                        "Nenhum lote encontrado para este produto."
                )
        );


        // ============================================================
        // COLUNA LOTE
        // ============================================================

        TableColumn<LoteProduto, String> colLote =
                new TableColumn<>(
                        "LOTE"
                );


        colLote.setPrefWidth(
                130
        );


        colLote.setCellValueFactory(
                new PropertyValueFactory<>(
                        "codigoLote"
                )
        );


        // ============================================================
        // COLUNA QUANTIDADE
        // ============================================================

        TableColumn<LoteProduto, Integer> colQuantidadeLote =
                new TableColumn<>(
                        "QUANTIDADE"
                );


        colQuantidadeLote.setPrefWidth(
                120
        );


        colQuantidadeLote.setCellValueFactory(
                new PropertyValueFactory<>(
                        "quantidade"
                )
        );


        // ============================================================
        // COLUNA CUSTO
        // ============================================================

        TableColumn<LoteProduto, BigDecimal> colCusto =
                new TableColumn<>(
                        "CUSTO UNITÁRIO"
                );


        colCusto.setPrefWidth(
                150
        );


        colCusto.setCellValueFactory(
                new PropertyValueFactory<>(
                        "custoUnitario"
                )
        );


        colCusto.setCellFactory(
                coluna -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            BigDecimal valor,
                            boolean empty) {

                        super.updateItem(
                                valor,
                                empty
                        );


                        if (empty
                                || valor == null) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    moedaBrasil.format(
                                            valor
                                    )
                            );
                        }
                    }
                }
        );


        // ============================================================
        // COLUNA DATA
        // ============================================================

        TableColumn<LoteProduto, java.time.LocalDateTime> colData =
                new TableColumn<>(
                        "ENTRADA"
                );


        colData.setPrefWidth(
                180
        );


        colData.setCellValueFactory(
                new PropertyValueFactory<>(
                        "dataEntrada"
                )
        );


        colData.setCellFactory(
                coluna -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            java.time.LocalDateTime data,
                            boolean empty) {

                        super.updateItem(
                                data,
                                empty
                        );


                        if (empty
                                || data == null) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    data.format(
                                            formatoData
                                    )
                            );
                        }
                    }
                }
        );


        tabela.getColumns().addAll(
                colLote,
                colQuantidadeLote,
                colCusto,
                colData
        );


        tabela.setItems(
                FXCollections.observableArrayList(
                        lotes
                )
        );


        // ============================================================
        // RESUMO DOS LOTES
        // ============================================================

        int quantidadeTotal =
                0;


        BigDecimal valorTotal =
                BigDecimal.ZERO;


        for (LoteProduto lote : lotes) {

            quantidadeTotal +=
                    lote.getQuantidade();


            BigDecimal valorLote =
                    lote.getCustoUnitario()
                            .multiply(
                                    BigDecimal.valueOf(
                                            lote.getQuantidade()
                                    )
                            );


            valorTotal =
                    valorTotal.add(
                            valorLote
                    );
        }


        Label lblResumo =
                new Label(
                        "Estoque nos lotes: "
                                + quantidadeTotal
                                + " unidade(s)"
                                + "   |   Valor: "
                                + moedaBrasil.format(
                                        valorTotal
                                )
                );


        GridPane conteudo =
                new GridPane();


        conteudo.setVgap(
                12
        );


        conteudo.setPadding(
                new Insets(
                        10
                )
        );


        conteudo.add(
                tabela,
                0,
                0
        );


        conteudo.add(
                lblResumo,
                0,
                1
        );


        dialog
                .getDialogPane()
                .setContent(
                        conteudo
                );


        dialog
                .getDialogPane()
                .getButtonTypes()
                .add(
                        ButtonType.CLOSE
                );


        dialog.showAndWait();
    }


    // ============================================================
    // EDITAR PRODUTO
    // ============================================================

    private void editarProduto(
            Produto produto) {


        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Editar produto"
        );


        dialog.setHeaderText(
                "Editar produto #"
                        + produto.getId()
                        + " - "
                        + produto.getNome()
        );


        // ============================================================
        // CAMPOS QUE PERTENCEM AO PRODUTO
        // ============================================================

        TextField txtNome =
                new TextField(
                        produto.getNome()
                );


        TextField txtCodigo =
                new TextField(
                        String.valueOf(
                                produto.getCodigo()
                        )
                );


        TextField txtDescricao =
                new TextField(
                        produto.getDescricao()
                                == null
                                ? ""
                                : produto.getDescricao()
                );


        TextField txtPrecoVenda =
                new TextField(
                        String.valueOf(
                                produto.getPrecoVenda()
                        )
                );


        txtCodigo
                .textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            if (!novo.matches(
                                    "\\d*"
                            )) {

                                txtCodigo.setText(
                                        novo.replaceAll(
                                                "[^\\d]",
                                                ""
                                        )
                                );
                            }
                        }
                );


        txtPrecoVenda
                .textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            if (!novo.matches(
                                    "\\d*([,.]\\d*)?"
                            )) {

                                txtPrecoVenda.setText(
                                        antigo
                                );
                            }
                        }
                );


        GridPane grid =
                new GridPane();


        grid.setHgap(
                12
        );


        grid.setVgap(
                12
        );


        grid.setPadding(
                new Insets(
                        20,
                        20,
                        10,
                        20
                )
        );


        grid.add(
                new Label(
                        "Nome:"
                ),
                0,
                0
        );


        grid.add(
                txtNome,
                1,
                0
        );


        grid.add(
                new Label(
                        "Código:"
                ),
                0,
                1
        );


        grid.add(
                txtCodigo,
                1,
                1
        );


        grid.add(
                new Label(
                        "Descrição:"
                ),
                0,
                2
        );


        grid.add(
                txtDescricao,
                1,
                2
        );


        grid.add(
                new Label(
                        "Preço de venda:"
                ),
                0,
                3
        );


        grid.add(
                txtPrecoVenda,
                1,
                3
        );


        Label avisoEstoque =
                new Label(
                        "Quantidade, lote e custo são controlados pela entrada de estoque."
                );


        grid.add(
                avisoEstoque,
                0,
                4,
                2,
                1
        );


        txtNome.setPrefWidth(
                300
        );


        dialog
                .getDialogPane()
                .setContent(
                        grid
                );


        ButtonType btnSalvar =
                new ButtonType(
                        "Salvar alterações",
                        ButtonBar.ButtonData.OK_DONE
                );


        ButtonType btnCancelar =
                new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );


        dialog
                .getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnSalvar,
                        btnCancelar
                );


        Button salvarButton =
                (Button) dialog
                        .getDialogPane()
                        .lookupButton(
                                btnSalvar
                        );


        salvarButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {


                    if (txtNome
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o nome do produto."
                        );


                        event.consume();

                        return;
                    }


                    if (txtCodigo
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o código do produto."
                        );


                        event.consume();

                        return;
                    }


                    if (txtPrecoVenda
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o preço de venda."
                        );


                        event.consume();

                        return;
                    }


                    try {

                        double precoVenda =
                                Double.parseDouble(
                                        txtPrecoVenda
                                                .getText()
                                                .trim()
                                                .replace(
                                                        ",",
                                                        "."
                                                )
                                );


                        if (precoVenda < 0) {

                            AlertaUtil.erro(
                                    "Valor inválido",
                                    "O preço de venda não pode ser negativo."
                            );


                            event.consume();
                        }


                    } catch (NumberFormatException e) {

                        AlertaUtil.erro(
                                "Valor inválido",
                                "Verifique o preço de venda informado."
                        );


                        event.consume();
                    }
                }
        );


        Optional<ButtonType> resultado =
                dialog.showAndWait();


        if (resultado.isPresent()
                && resultado.get()
                == btnSalvar) {


            try {

                produto.setNome(
                        txtNome
                                .getText()
                                .trim()
                );


                produto.setCodigo(
                        Integer.parseInt(
                                txtCodigo
                                        .getText()
                                        .trim()
                        )
                );


                produto.setDescricao(
                        txtDescricao
                                .getText()
                                .trim()
                );


                produto.setPrecoVenda(
                        Double.parseDouble(
                                txtPrecoVenda
                                        .getText()
                                        .trim()
                                        .replace(
                                                ",",
                                                "."
                                        )
                        )
                );


                /*
                 * NÃO alteramos:
                 *
                 * produto.setLote(...)
                 * produto.setPrecoCusto(...)
                 * produto.setQtdeEstoque(...)
                 *
                 * Esses dados agora são controlados
                 * através dos lotes/entradas de estoque.
                 */


                String retorno =
                        produtoDAO.atualizar(
                                produto
                        );


                if (retorno.contains(
                        "sucesso"
                )) {

                    carregarProdutos();


                    lblStatus.setText(
                            "Produto atualizado com sucesso"
                    );


                    AlertaUtil.sucesso(
                            "Produto atualizado",
                            "As alterações foram salvas com sucesso!"
                    );


                } else {

                    AlertaUtil.erro(
                            "Erro",
                            retorno
                    );
                }


            } catch (Exception e) {

                AlertaUtil.erro(
                        "Erro",
                        "Não foi possível atualizar o produto.\n\n"
                                + e.getMessage()
                );


                e.printStackTrace();
            }
        }
    }


    // ============================================================
    // EXCLUIR PRODUTO
    // ============================================================

    private void excluirProduto(
            Produto produto) {


        boolean confirmou =
                AlertaUtil.confirmar(
                        "Confirmar exclusão",
                        "Deseja realmente excluir o produto \""
                                + produto.getNome()
                                + "\"?"
                );


        if (!confirmou) {

            return;
        }


        try {

            String retorno =
                    produtoDAO.deletar(
                            produto.getId()
                    );


            if (retorno.contains(
                    "sucesso"
            )) {

                carregarProdutos();


                lblStatus.setText(
                        "Produto removido com sucesso"
                );


                AlertaUtil.sucesso(
                        "Produto removido",
                        "Produto removido com sucesso!"
                );


            } else {

                lblStatus.setText(
                        "Erro ao remover produto"
                );


                AlertaUtil.erro(
                        "Erro",
                        retorno
                );
            }


        } catch (Exception e) {

            lblStatus.setText(
                    "Erro ao remover produto"
            );


            AlertaUtil.erro(
                    "Erro",
                    "Erro ao excluir produto: "
                            + e.getMessage()
            );


            e.printStackTrace();
        }
    }


    // ============================================================
    // RESUMO DO ESTOQUE
    // ============================================================

    private void atualizarResumo(
            ObservableList<Produto> lista) {


        int totalProdutos =
                lista.size();


        BigDecimal valorTotalEstoque =
                BigDecimal.ZERO;


        /*
         * Agora o valor do estoque não usa mais
         * produto.precoCusto.
         *
         * O cálculo é feito utilizando:
         *
         * quantidade do lote × custo unitário do lote.
         */

        for (Produto produto : lista) {

            List<LoteProduto> lotes =
                    loteProdutoDAO
                            .buscarPorProduto(
                                    produto.getId()
                            );


            for (LoteProduto lote : lotes) {

                BigDecimal valorLote =
                        lote.getCustoUnitario()
                                .multiply(
                                        BigDecimal.valueOf(
                                                lote.getQuantidade()
                                        )
                                );


                valorTotalEstoque =
                        valorTotalEstoque.add(
                                valorLote
                        );
            }
        }


        lblTotalItens.setText(
                String.valueOf(
                        totalProdutos
                )
        );


        String valorFormatado =
                moedaBrasil.format(
                        valorTotalEstoque
                );


        valorFormatado =
                valorFormatado
                        .replace(
                                "R$",
                                ""
                        )
                        .trim();


        lblValorTotal.setText(
                valorFormatado
        );
    }
}