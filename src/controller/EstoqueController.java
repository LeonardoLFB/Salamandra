package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.Optional;

/**
 * Controller para TelaEstoque.fxml
 */
public class EstoqueController {

    // ===== FXML injections (devem bater com fx:id do FXML) =====
    @FXML private TextField txtNomeProduto;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPrecoCusto;
    @FXML private TextField txtPrecoVenda;
    @FXML private ComboBox<String> cbStatusProduto;
    @FXML private Button btnCadastrarProduto;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroStatus;
    @FXML private Button btnNovoProduto;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colNomeProduto;
    @FXML private TableColumn<Product, String> colCodigo;
    @FXML private TableColumn<Product, Integer> colQuantidade;
    @FXML private TableColumn<Product, Double> colPreco;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Void> colAcoes;

    @FXML private Label lblStatus;
    @FXML private Label lblTotalItens;
    @FXML private Label lblValorTotal;

    // ===== Dados e utilitários =====
    private final ObservableList<Product> data = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        // segurança básica (útil durante desenvolvimento)
        if (colNomeProduto == null) {
            System.err.println("FXML injection failed: verifique fx:id e package do controller.");
            return;
        }

        // configurar colunas
        colNomeProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // formatação da coluna de preço (2 casas decimais)
        colPreco.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", value));
                }
            }
        });

        // formatação do status como badge (Label com estilo)
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.setStyle("-fx-padding:4 10; -fx-background-radius:12; -fx-font-size:11;");
                    String s = status.toLowerCase();
                    if (s.contains("em estoque")) {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#dcfce7; -fx-text-fill:#065f46;");
                    } else if (s.contains("baixo")) {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#fff7d6; -fx-text-fill:#92400e;");
                    } else if (s.contains("fora")) {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#ffe4e6; -fx-text-fill:#991b1b;");
                    } else {
                        badge.setStyle(badge.getStyle() + " -fx-background-color:#eef2ff; -fx-text-fill:#3730a3;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // coluna de ações (editar / remover)
        addActionsColumn();

        // popular combos
        cbStatusProduto.getItems().addAll("Em estoque", "Estoque baixo", "Fora de estoque");
        cbStatusProduto.getSelectionModel().selectFirst();

        cbFiltroStatus.getItems().addAll("Todos", "Em estoque", "Estoque baixo", "Fora de estoque");
        cbFiltroStatus.getSelectionModel().select("Todos");

        // dados de exemplo
        data.addAll(
                new Product("Incenso de Sândalo", "INC001", 50, 5.50, 9.90, "Em estoque"),
                new Product("Incenso de Lavanda", "INC002", 15, 4.50, 8.50, "Estoque baixo"),
                new Product("Incenso de Mirra", "INC003", 0, 6.50, 12.00, "Fora de estoque")
        );

        productTable.setItems(data);

        // listeners / eventos
        btnCadastrarProduto.setOnAction(e -> onCadastrarProduto());
        btnNovoProduto.setOnAction(e -> limparFormulario());
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltro());
        cbFiltroStatus.setOnAction(e -> aplicarFiltro());

        atualizarResumo();
    }

    // ===== Ações =====

    private void onCadastrarProduto() {
        // validação simples
        String nome = txtNomeProduto.getText() == null ? "" : txtNomeProduto.getText().trim();
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String qtdText = txtQuantidade.getText() == null ? "0" : txtQuantidade.getText().trim();
        String precoVendaText = txtPrecoVenda.getText() == null ? "0" : txtPrecoVenda.getText().trim();
        String status = cbStatusProduto.getValue();

        if (nome.isEmpty() || codigo.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Nome e código são obrigatórios.").showAndWait();
            return;
        }

        int qtd;
        double precoVenda;
        try {
            qtd = Integer.parseInt(qtdText);
            precoVenda = Double.parseDouble(precoVendaText.replace(",", "."));
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.WARNING, "Quantidade ou preço inválido.").showAndWait();
            return;
        }

        Product existente = encontrarPorCodigo(codigo);
        if (existente != null) {
            // se já existe, atualiza quantidade e preço
            existente.setQuantidade(existente.getQuantidade() + qtd);
            existente.setPreco(precoVenda);
            productTable.refresh();
        } else {
            Product p = new Product(nome, codigo, qtd, 0.0, precoVenda, status);
            data.add(p);
            productTable.setItems(data);
        }

        limparFormulario();
        atualizarResumo();
        lblStatus.setText("Produto cadastrado/atualizado");
    }

    private void limparFormulario() {
        txtNomeProduto.clear();
        txtCodigo.clear();
        txtQuantidade.clear();
        txtPrecoCusto.clear();
        txtPrecoVenda.clear();
        cbStatusProduto.getSelectionModel().selectFirst();
        lblStatus.setText("Novo produto");
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String statusFiltro = cbFiltroStatus.getValue();
        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product p : data) {
            boolean matchesQ = p.getNome().toLowerCase().contains(q) || p.getCodigo().toLowerCase().contains(q);
            boolean matchesStatus = statusFiltro == null || statusFiltro.equals("Todos") || p.getStatus().equalsIgnoreCase(statusFiltro);
            if (matchesQ && matchesStatus) filtered.add(p);
        }
        productTable.setItems(filtered);
        atualizarResumo(filtered);
    }

    private Product encontrarPorCodigo(String codigo) {
        for (Product p : data) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) return p;
        }
        return null;
    }

    // ===== Coluna de ações =====
    private void addActionsColumn() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDel = new Button("🗑");
            private final HBox pane = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
                btnDel.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

                btnEdit.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    showEditDialog(p);
                });

                btnDel.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover produto " + p.getNome() + " ?");
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        data.remove(p);
                        productTable.setItems(data);
                        atualizarResumo();
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

    private void showEditDialog(Product p) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Editar produto");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nome = new TextField(p.getNome());
        TextField codigo = new TextField(p.getCodigo());
        TextField qtd = new TextField(String.valueOf(p.getQuantidade()));
        TextField preco = new TextField(String.format("%.2f", p.getPreco()));
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Em estoque", "Estoque baixo", "Fora de estoque"));
        status.getSelectionModel().select(p.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, new Label("Nome:"), nome);
        grid.addRow(1, new Label("Código:"), codigo);
        grid.addRow(2, new Label("Quantidade:"), qtd);
        grid.addRow(3, new Label("Preço (venda):"), preco);
        grid.addRow(4, new Label("Status:"), status);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    p.setNome(nome.getText().trim());
                    p.setCodigo(codigo.getText().trim());
                    p.setQuantidade(Integer.parseInt(qtd.getText().trim()));
                    p.setPreco(Double.parseDouble(preco.getText().trim().replace(",", ".")));
                    p.setStatus(status.getValue());
                    productTable.refresh();
                    atualizarResumo();
                    return p;
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Dados inválidos").showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ===== Resumo =====
    private void atualizarResumo() {
        atualizarResumo(this.data);
    }

    private void atualizarResumo(ObservableList<Product> lista) {
        int totalItens = 0;
        double valorTotal = 0.0;
        for (Product p : lista) {
            totalItens += p.getQuantidade();
            valorTotal += p.getQuantidade() * p.getPreco();
        }
        lblTotalItens.setText(String.valueOf(totalItens));
        lblValorTotal.setText(String.format("%.2f", valorTotal));
    }

    // ===== Modelo simples (substitua pela sua classe Produto se desejar) =====
    public static class Product {
        private String nome;
        private String codigo;
        private int quantidade;
        private double precoCusto;
        private double preco;
        private String status;

        public Product(String nome, String codigo, int quantidade, double precoCusto, double preco, String status) {
            this.nome = nome;
            this.codigo = codigo;
            this.quantidade = quantidade;
            this.precoCusto = precoCusto;
            this.preco = preco;
            this.status = status;
        }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

        public double getPrecoCusto() { return precoCusto; }
        public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }

        public double getPreco() { return preco; }
        public void setPreco(double preco) { this.preco = preco; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
