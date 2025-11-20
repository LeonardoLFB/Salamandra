package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class ProdutosController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbCategoria; // <-- nome alinhado com seu FXML
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnAdd;

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, String> colNome;
    @FXML private TableColumn<Product, String> colDescricao;
    @FXML private TableColumn<Product, Double> colPreco;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Void> colAcoes;

    private final ObservableList<Product> data = FXCollections.observableArrayList();
    @SuppressWarnings("deprecation")
	private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));

    @FXML
    private void initialize() {
        // proteções: evite NullPointer ao depurar — verifique injeções
        if (colNome == null) {
            System.err.println("ERROR: colNome not injected. Check fx:id in FXML and controller package/name.");
            return;
        }

        // setup columns
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPreco.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText((empty || value == null) ? null : currency.format(value));
            }
        });

        // status badge
        colStatus.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.setStyle("-fx-padding:4 8; -fx-background-radius:12; -fx-font-size:11;");
                    String s = status.toLowerCase();
                    if (s.contains("em estoque")) badge.setStyle(badge.getStyle() + " -fx-background-color:#dcfce7; -fx-text-fill:#065f46;");
                    else if (s.contains("baixo")) badge.setStyle(badge.getStyle() + " -fx-background-color:#fff4e6; -fx-text-fill:#92400e;");
                    else if (s.contains("fora")) badge.setStyle(badge.getStyle() + " -fx-background-color:#ffe4e6; -fx-text-fill:#991b1b;");
                    else badge.setStyle(badge.getStyle() + " -fx-background-color:#eef2ff; -fx-text-fill:#3730a3;");
                    setGraphic(badge);
                }
            }
        });

        addActionsColumn();

        // exemplo de dados
        data.addAll(
            new Product("Sândalo", "Aroma amadeirado clássico...", 12.50, "Em estoque"),
            new Product("Lavanda", "Propriedades calmantes...", 10.00, "Em estoque"),
            new Product("Nag Champa", "Fragrância floral...", 15.00, "Estoque baixo")
        );

        tableProducts.setItems(data);

        // popular filtros (use cbCategoria, cbStatus)
        cbCategoria.getItems().addAll("Todos","Incenso","Vela","Essência");
        cbStatus.getItems().addAll("Todos","Em estoque","Estoque baixo","Fora de estoque");
        cbCategoria.getSelectionModel().select("Todos");
        cbStatus.getSelectionModel().select("Todos");

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbCategoria.setOnAction(e -> applyFilters());
        cbStatus.setOnAction(e -> applyFilters());
        btnAdd.setOnAction(e -> onAdd());
    }

    private void applyFilters() {
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
        String categoria = cbCategoria.getValue();
        String status = cbStatus.getValue();

        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product p : data) {
            boolean matchesQ = p.getNome().toLowerCase().contains(q) || p.getDescricao().toLowerCase().contains(q);
            boolean matchesCategoria = categoria == null || categoria.equals("Todos") || p.getTipo().equals(categoria);
            boolean matchesStatus = status == null || status.equals("Todos") || p.getStatus().equalsIgnoreCase(status);
            if (matchesQ && matchesCategoria && matchesStatus) filtered.add(p);
        }
        tableProducts.setItems(filtered);
    }

    private void onAdd() {
        // implementação simples (pode abrir dialog)
        Product p = new Product("Novo", "Descrição", 0.0, "Em estoque");
        data.add(p);
        tableProducts.setItems(data);
    }

    private void addActionsColumn() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = param -> new TableCell<>() {
            private final Button edit = new Button("✎");
            private final Button del = new Button("🗑");
            private final HBox pane = new HBox(8, edit, del);
            {
                edit.setStyle("-fx-background-color:transparent;");
                del.setStyle("-fx-background-color:transparent;");
                edit.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    // abrir diálogo de edição...
                });
                del.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    data.remove(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
        colAcoes.setCellFactory(cellFactory);
    }

    // modelo simples
    public static class Product {
        private String nome;
        private String descricao;
        private double preco;
        private String status;
        private String tipo = "Incenso";

        public Product(String nome, String descricao, double preco, String status) {
            this.nome = nome; this.descricao = descricao; this.preco = preco; this.status = status;
        }
        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
        public double getPreco() { return preco; }
        public String getStatus() { return status; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public void setPreco(double p) { this.preco = p; }
        public void setNome(String n) { this.nome = n; }
        public void setDescricao(String d) { this.descricao = d; }
        public void setStatus(String s) { this.status = s; }
    }
}
