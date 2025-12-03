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

import java.util.List;
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

    private ObservableList<Produto> data = FXCollections.observableArrayList();
    private ObservableList<Produto> dataOriginal = FXCollections.observableArrayList();
    private ProdutoDAO produtoDAO = new ProdutoDAO();

    @FXML
    private void initialize() {
        // Verificação de segurança
        if (colNomeProduto == null) {
            System.err.println("FXML injection failed: verifique fx:id e package do controller.");
            return;
        }

        // Configurar colunas
        colNomeProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("qtdeEstoque"));
        colPrecoCusto.setCellValueFactory(new PropertyValueFactory<>("precoCusto"));
        colPrecoVenda.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));

        // Formatação das colunas de preço (2 casas decimais)
        colPrecoCusto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", value));
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
                    setText(String.format("R$ %.2f", value));
                }
            }
        });

        // Coluna de ações
        addActionsColumn();

        // Popular combo de filtro
        cbFiltroStatus.getItems().addAll("Todos", "Em estoque", "Estoque baixo", "Fora de estoque");
        cbFiltroStatus.getSelectionModel().select("Todos");

        // Carregar dados do banco
        carregarProdutos();

        // Listeners
        btnAtualizar.setOnAction(e -> carregarProdutos());
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltro());
        cbFiltroStatus.setOnAction(e -> aplicarFiltro());
    }

    private void carregarProdutos() {
        try {
            List<Produto> produtos = produtoDAO.getAll();
            
            if (produtos != null && !produtos.isEmpty()) {
                dataOriginal.setAll(produtos);
                data.setAll(produtos);
                productTable.setItems(data);
                atualizarResumo();
                lblStatus.setText("Produtos carregados: " + produtos.size());
            } else {
                dataOriginal.clear();
                data.clear();
                productTable.setItems(data);
                lblStatus.setText("Nenhum produto encontrado");
                atualizarResumo();
            }
        } catch (Exception e) {
            lblStatus.setText("Erro ao carregar produtos");
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao carregar produtos do banco: " + e.getMessage()).showAndWait();
        }
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String statusFiltro = cbFiltroStatus.getValue();
        
        ObservableList<Produto> filtered = FXCollections.observableArrayList();
        
        for (Produto p : dataOriginal) {
            // Filtro de busca por nome ou código
            boolean matchesQ = p.getNome().toLowerCase().contains(q) || 
                             String.valueOf(p.getCodigo()).contains(q);
            
            // Filtro de status baseado na quantidade em estoque
            boolean matchesStatus = true;
            if (statusFiltro != null && !statusFiltro.equals("Todos")) {
                int qtde = p.getQtdeEstoque();
                if (statusFiltro.equals("Em estoque")) {
                    matchesStatus = qtde > 10;
                } else if (statusFiltro.equals("Estoque baixo")) {
                    matchesStatus = qtde > 0 && qtde <= 10;
                } else if (statusFiltro.equals("Fora de estoque")) {
                    matchesStatus = qtde == 0;
                }
            }
            
            if (matchesQ && matchesStatus) {
                filtered.add(p);
            }
        }
        
        data.setAll(filtered);
        productTable.setItems(data);
        atualizarResumo(filtered);
    }

    private void addActionsColumn() {
        Callback<TableColumn<Produto, Void>, TableCell<Produto, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnDel = new Button("🗑");
            private final HBox pane = new HBox(8, btnDel);

            {
                btnDel.setStyle("-fx-background-color:transparent; -fx-cursor:hand; -fx-font-size:14;");

                btnDel.setOnAction(e -> {
                    Produto p = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                        "Tem certeza que deseja remover o produto '" + p.getNome() + "'?");
                    Optional<ButtonType> res = confirm.showAndWait();
                    
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        String resultado = produtoDAO.deletar(p.getId());
                        
                        if (resultado.contains("sucesso")) {
                            carregarProdutos(); // Recarrega a lista
                            lblStatus.setText("Produto removido");
                            new Alert(Alert.AlertType.INFORMATION, "Produto removido com sucesso!").showAndWait();
                        } else {
                            lblStatus.setText("Erro ao remover");
                            new Alert(Alert.AlertType.ERROR, resultado).showAndWait();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(pane);
            }
        };
        colAcoes.setCellFactory(cellFactory);
    }

    private void atualizarResumo() {
        atualizarResumo(this.data);
    }

    private void atualizarResumo(ObservableList<Produto> lista) {
        int totalProdutos = lista.size();
        int totalItens = 0;
        double valorTotal = 0.0;
        
        for (Produto p : lista) {
            totalItens += p.getQtdeEstoque();
            valorTotal += p.getQtdeEstoque() * (p.getPrecoVenda() - p.getPrecoCusto());
        }
        
        lblTotalItens.setText(String.valueOf(totalProdutos));
        lblValorTotal.setText(String.format("%.2f", valorTotal));
    }
}