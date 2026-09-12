package controller;

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
import model.Produto;
import util.AlertaUtil;

import java.text.NumberFormat;
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
    private TableColumn<Produto, Double> colPrecoCusto;

    @FXML
    private TableColumn<Produto, Double> colPrecoVenda;

    @FXML
    private TableColumn<Produto, String> colLote;

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

    private final NumberFormat moedaBrasil =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );

    @FXML
    private void initialize() {

        configurarColunas();
        configurarColunaQuantidade();
        configurarColunasPreco();
        addActionsColumn();

        productTable.setItems(data);

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
                .select("Todos");

        txtBuscar
                .textProperty()
                .addListener(
                        (obs, oldV, newV) ->
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

        colPrecoCusto.setCellValueFactory(
                new PropertyValueFactory<>(
                        "precoCusto"
                )
        );

        colPrecoVenda.setCellValueFactory(
                new PropertyValueFactory<>(
                        "precoVenda"
                )
        );

        colLote.setCellValueFactory(
                new PropertyValueFactory<>(
                        "lote"
                )
        );
    }

    private void configurarColunasPreco() {

        colPrecoCusto.setCellFactory(
                col -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Double value,
                            boolean empty) {

                        super.updateItem(
                                value,
                                empty
                        );

                        if (empty || value == null) {

                            setText(null);

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

                        if (empty || value == null) {

                            setText(null);

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

                        setGraphic(null);
                        setText(null);

                        if (empty
                                || quantidade == null) {

                            return;
                        }

                        Label badge =
                                new Label();

                        badge.getStyleClass()
                                .add(
                                        "stock-badge"
                                );

                        if (quantidade == 0) {

                            badge.setText(
                                    "0 - Sem estoque"
                            );

                            badge.getStyleClass()
                                    .add(
                                            "stock-badge-empty"
                                    );

                        } else if (quantidade <= 10) {

                            badge.setText(
                                    String.valueOf(
                                            quantidade
                                    )
                            );

                            badge.getStyleClass()
                                    .add(
                                            "stock-badge-low"
                                    );

                        } else {

                            badge.setText(
                                    String.valueOf(
                                            quantidade
                                    )
                            );

                            badge.getStyleClass()
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
                                    .contains(busca)
                    )

                            || String
                            .valueOf(
                                    produto.getCodigo()
                            )
                            .contains(busca);

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

    private void addActionsColumn() {

        Callback<TableColumn<Produto, Void>,
                TableCell<Produto, Void>>
                cellFactory = param ->
                new TableCell<>() {

                    private final Button btnEditar =
                            new Button("✏");

                    private final Button btnExcluir =
                            new Button("🗑");

                    private final HBox box =
                            new HBox(
                                    6,
                                    btnEditar,
                                    btnExcluir
                            );

                    {

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
                                            getTableView()
                                                    .getItems()
                                                    .get(
                                                            getIndex()
                                                    );

                                    editarProduto(
                                            produto
                                    );
                                }
                        );

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
                                            getTableView()
                                                    .getItems()
                                                    .get(
                                                            getIndex()
                                                    );

                                    excluirProduto(
                                            produto
                                    );
                                }
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

        TextField txtLote =
                new TextField(
                        produto.getLote()
                );

        TextField txtDescricao =
                new TextField(
                        produto.getDescricao()
                );

        TextField txtPrecoCusto =
                new TextField(
                        String.valueOf(
                                produto.getPrecoCusto()
                        )
                );

        TextField txtPrecoVenda =
                new TextField(
                        String.valueOf(
                                produto.getPrecoVenda()
                        )
                );

        TextField txtQuantidade =
                new TextField(
                        String.valueOf(
                                produto.getQtdeEstoque()
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

        txtLote
                .textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            if (novo.length() > 5) {

                                txtLote.setText(
                                        antigo
                                );
                            }
                        }
                );

        txtPrecoCusto
                .textProperty()
                .addListener(
                        (obs, antigo, novo) -> {

                            if (!novo.matches(
                                    "\\d*([,.]\\d*)?"
                            )) {

                                txtPrecoCusto.setText(
                                        antigo
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
                new Label("Nome:"),
                0,
                0
        );

        grid.add(
                txtNome,
                1,
                0
        );

        grid.add(
                new Label("Código:"),
                0,
                1
        );

        grid.add(
                txtCodigo,
                1,
                1
        );

        grid.add(
                new Label("Lote:"),
                0,
                2
        );

        grid.add(
                txtLote,
                1,
                2
        );

        grid.add(
                new Label("Descrição:"),
                0,
                3
        );

        grid.add(
                txtDescricao,
                1,
                3
        );

        grid.add(
                new Label("Preço de custo:"),
                0,
                4
        );

        grid.add(
                txtPrecoCusto,
                1,
                4
        );

        grid.add(
                new Label("Preço de venda:"),
                0,
                5
        );

        grid.add(
                txtPrecoVenda,
                1,
                5
        );

        grid.add(
                new Label("Quantidade:"),
                0,
                6
        );

        grid.add(
                txtQuantidade,
                1,
                6
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

                    if (txtLote
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o lote do produto."
                        );

                        event.consume();

                        return;
                    }

                    if (txtPrecoCusto
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe o preço de custo."
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

                    if (txtQuantidade
                            .getText()
                            .trim()
                            .isEmpty()) {

                        AlertaUtil.erro(
                                "Campo obrigatório",
                                "Informe a quantidade em estoque."
                        );

                        event.consume();

                        return;
                    }

                    try {

                        double precoCusto =
                                Double.parseDouble(
                                        txtPrecoCusto
                                                .getText()
                                                .trim()
                                                .replace(
                                                        ",",
                                                        "."
                                                )
                                );

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

                        int quantidade =
                                Integer.parseInt(
                                        txtQuantidade
                                                .getText()
                                                .trim()
                                );

                        if (precoCusto < 0) {

                            AlertaUtil.erro(
                                    "Valor inválido",
                                    "O preço de custo não pode ser negativo."
                            );

                            event.consume();

                            return;
                        }

                        if (precoVenda < 0) {

                            AlertaUtil.erro(
                                    "Valor inválido",
                                    "O preço de venda não pode ser negativo."
                            );

                            event.consume();

                            return;
                        }

                        if (quantidade < 0) {

                            AlertaUtil.erro(
                                    "Valor inválido",
                                    "A quantidade não pode ser negativa."
                            );

                            event.consume();
                        }

                    } catch (NumberFormatException e) {

                        AlertaUtil.erro(
                                "Valor inválido",
                                "Verifique os campos numéricos."
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

                produto.setLote(
                        txtLote
                                .getText()
                                .trim()
                );

                produto.setDescricao(
                        txtDescricao
                                .getText()
                                .trim()
                );

                produto.setPrecoCusto(
                        Double.parseDouble(
                                txtPrecoCusto
                                        .getText()
                                        .trim()
                                        .replace(
                                                ",",
                                                "."
                                        )
                        )
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

                produto.setQtdeEstoque(
                        Integer.parseInt(
                                txtQuantidade
                                        .getText()
                                        .trim()
                        )
                );

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

    private void atualizarResumo(
            ObservableList<Produto> lista) {

        int totalProdutos =
                lista.size();

        double valorTotalEstoque =
                0.0;

        for (Produto produto : lista) {

            valorTotalEstoque +=
                    produto.getQtdeEstoque()
                            * produto.getPrecoCusto();
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