package controller;

import database.ProdutoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.Produto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class EstoqueController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroStatus;
    @FXML private Button btnAtualizar;

    @FXML private TableView<Produto> productTable;
    @FXML private TableColumn<Produto, String> colNomeProduto;
    @FXML private TableColumn<Produto, Integer> colCodigo;
    @FXML private TableColumn<Produto, Integer> colQuantidade;
    @FXML private TableColumn<Produto, Double> colPrecoCusto;
    @FXML private TableColumn<Produto, Double> colPrecoVenda;
    @FXML private TableColumn<Produto, String> colLote;
    @FXML private TableColumn<Produto, Void> colAcoes;

    @FXML private Label lblStatus;
    @FXML private Label lblTotalItens;
    @FXML private Label lblValorTotal;

    private final ObservableList<Produto> data = FXCollections.observableArrayList();
    private final ObservableList<Produto> dataOriginal = FXCollections.observableArrayList();

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    private final NumberFormat moedaBrasil =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    private void initialize() {

        configurarColunas();
        configurarColunaQuantidade();
        configurarColunasPreco();
        addActionsColumn();

        productTable.setItems(data);
        productTable.setPlaceholder(new Label("Nenhum produto encontrado."));

        cbFiltroStatus.getItems().addAll(
                "Todos",
                "Em estoque",
                "Estoque baixo",
                "Fora de estoque"
        );

        cbFiltroStatus.getSelectionModel().select("Todos");

        txtBuscar.textProperty().addListener(
                (obs, oldV, newV) -> aplicarFiltro()
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
                new PropertyValueFactory<>("nome")
        );

        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );

        colQuantidade.setCellValueFactory(
                new PropertyValueFactory<>("qtdeEstoque")
        );

        colPrecoCusto.setCellValueFactory(
                new PropertyValueFactory<>("precoCusto")
        );

        colPrecoVenda.setCellValueFactory(
                new PropertyValueFactory<>("precoVenda")
        );

        colLote.setCellValueFactory(
                new PropertyValueFactory<>("lote")
        );
    }

    private void configurarColunasPreco() {

        colPrecoCusto.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Double value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(moedaBrasil.format(value));
                }
            }
        });

        colPrecoVenda.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Double value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(moedaBrasil.format(value));
                }
            }
        });
    }

    private void configurarColunaQuantidade() {

        colQuantidade.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Integer quantidade, boolean empty) {

                super.updateItem(quantidade, empty);

                setGraphic(null);
                setText(null);

                if (empty || quantidade == null) {
                    return;
                }

                Label badge = new Label();

                badge.getStyleClass().add("stock-badge");

                if (quantidade == 0) {

                    badge.setText("0 - Sem estoque");
                    badge.getStyleClass().add("stock-badge-empty");

                } else if (quantidade <= 10) {

                    badge.setText(String.valueOf(quantidade));
                    badge.getStyleClass().add("stock-badge-low");

                } else {

                    badge.setText(String.valueOf(quantidade));
                    badge.getStyleClass().add("stock-badge-ok");
                }

                setGraphic(badge);
            }
        });
    }

    private void carregarProdutos() {

        try {

            List<Produto> produtos = produtoDAO.getAll();

            if (produtos != null) {

                dataOriginal.setAll(produtos);

            } else {

                dataOriginal.clear();
            }

            aplicarFiltro();

            lblStatus.setText(
                    "Produtos carregados: " + dataOriginal.size()
            );

        } catch (Exception e) {

            lblStatus.setText("Erro ao carregar produtos");

            showAlert(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Erro ao carregar produtos do banco: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void aplicarFiltro() {

        String busca = txtBuscar.getText() == null
                ? ""
                : txtBuscar.getText().trim().toLowerCase();

        String statusFiltro = cbFiltroStatus.getValue();

        ObservableList<Produto> filtrados =
                FXCollections.observableArrayList();

        for (Produto produto : dataOriginal) {

            boolean correspondeBusca =
                    busca.isEmpty()
                    || (
                        produto.getNome() != null
                        && produto.getNome()
                                .toLowerCase()
                                .contains(busca)
                    )
                    || String.valueOf(produto.getCodigo())
                             .contains(busca);

            boolean correspondeStatus = true;

            if (statusFiltro != null
                    && !statusFiltro.equals("Todos")) {

                int quantidade = produto.getQtdeEstoque();

                switch (statusFiltro) {

                    case "Em estoque":
                        correspondeStatus = quantidade > 10;
                        break;

                    case "Estoque baixo":
                        correspondeStatus =
                                quantidade > 0
                                && quantidade <= 10;
                        break;

                    case "Fora de estoque":
                        correspondeStatus = quantidade == 0;
                        break;

                    default:
                        correspondeStatus = true;
                        break;
                }
            }

            if (correspondeBusca && correspondeStatus) {
                filtrados.add(produto);
            }
        }

        data.setAll(filtrados);

        atualizarResumo(data);
    }

    private void addActionsColumn() {

        Callback<TableColumn<Produto, Void>, TableCell<Produto, Void>>
                cellFactory = param -> new TableCell<>() {

            private final Button btnExcluir =
                    new Button("🗑");

            private final HBox box =
                    new HBox(6, btnExcluir);

            {
                btnExcluir
                        .getStyleClass()
                        .add("table-action-delete");

                btnExcluir.setTooltip(
                        new Tooltip("Excluir produto")
                );

                btnExcluir.setOnAction(e -> {

                    Produto produto =
                            getTableView()
                            .getItems()
                            .get(getIndex());

                    excluirProduto(produto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(
                        empty ? null : box
                );
            }
        };

        colAcoes.setCellFactory(cellFactory);
    }

    private void excluirProduto(Produto produto) {

        Alert confirmacao =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText("Excluir produto?");

        confirmacao.setContentText(
                "Deseja realmente excluir o produto \""
                        + produto.getNome()
                        + "\"?"
        );

        Optional<ButtonType> resultado =
                confirmacao.showAndWait();

        if (resultado.isPresent()
                && resultado.get() == ButtonType.OK) {

            try {

                String retorno =
                        produtoDAO.deletar(produto.getId());

                if (retorno.contains("sucesso")) {

                    carregarProdutos();

                    lblStatus.setText(
                            "Produto removido com sucesso"
                    );

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Sucesso",
                            "Produto removido com sucesso!"
                    );

                } else {

                    lblStatus.setText(
                            "Erro ao remover produto"
                    );

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Erro",
                            retorno
                    );
                }

            } catch (Exception e) {

                lblStatus.setText(
                        "Erro ao remover produto"
                );

                showAlert(
                        Alert.AlertType.ERROR,
                        "Erro",
                        "Erro ao excluir produto: "
                                + e.getMessage()
                );

                e.printStackTrace();
            }
        }
    }

    private void atualizarResumo(
            ObservableList<Produto> lista) {

        int totalProdutos = lista.size();

        double valorTotalEstoque = 0.0;

        for (Produto produto : lista) {

            valorTotalEstoque +=
                    produto.getQtdeEstoque()
                    * produto.getPrecoCusto();
        }

        lblTotalItens.setText(
                String.valueOf(totalProdutos)
        );

        String valorFormatado =
                moedaBrasil.format(valorTotalEstoque);

        valorFormatado =
                valorFormatado.replace("R$", "").trim();

        lblValorTotal.setText(valorFormatado);
    }

    private void showAlert(
            Alert.AlertType tipo,
            String titulo,
            String mensagem) {

        Alert alert = new Alert(tipo);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}