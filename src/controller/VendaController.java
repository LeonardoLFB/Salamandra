package controller;

import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Venda;

public class VendaController {

    @FXML private TextField txtCliente;
    @FXML private TextField txtProdutos;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValor;
    @FXML private DatePicker datePickerVenda;
    @FXML private ComboBox<String> comboPagamento;

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> comboFiltroStatus;

    @FXML private TableView<Venda> tableVendas;
    @FXML private TableColumn<Venda, String> colId;
    @FXML private TableColumn<Venda, String> colCliente;
    @FXML private TableColumn<Venda, String> colData;
    @FXML private TableColumn<Venda, Double> colValor;
    @FXML private TableColumn<Venda, String> colStatus;
    @FXML private TableColumn<Venda, Void> colAcoes;

    private final ObservableList<Venda> vendas = FXCollections.observableArrayList();
    private int nextId = 1000;

    @FXML
    public void initialize() {

        comboPagamento.setItems(FXCollections.observableArrayList(
            "Dinheiro", "Cartão", "Pix", "Transferência"
        ));
        comboPagamento.getSelectionModel().selectFirst();

        comboFiltroStatus.setItems(FXCollections.observableArrayList(
            "Todos", "Pendente", "Concluída", "Cancelada"
        ));
        comboFiltroStatus.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colData.setCellValueFactory(new PropertyValueFactory<>("dataStr"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableVendas.setItems(vendas);

        // Exemplos iniciais
        vendas.add(new Venda(nextId++, "João da Silva", LocalDate.of(2025, 1, 10), 45.00, "Concluída"));
        vendas.add(new Venda(nextId++, "Maria Oliveira", LocalDate.of(2025, 3, 2), 22.50, "Pendente"));
    }

    @FXML
    private void onAdicionar(ActionEvent event) {

        String cliente = txtCliente.getText();
        String produtos = txtProdutos.getText();
        String qtdText = txtQuantidade.getText();
        String valorText = txtValor.getText();
        LocalDate data = datePickerVenda.getValue();
        String forma = comboPagamento.getValue();

        if (cliente == null || cliente.isBlank()) return;

        int quantidade = 1;
        try { quantidade = Integer.parseInt(qtdText.trim()); } catch (Exception e) { quantidade = 1; }

        double valor = 0.0;
        try { valor = Double.parseDouble(valorText.replace(',', '.')); } catch (Exception e) {}

        Venda v = new Venda(nextId++, cliente, data == null ? LocalDate.now() : data, valor, "Pendente");
        v.setProdutos(produtos);
        v.setQuantidade(quantidade);
        v.setFormaPagamento(forma);

        vendas.add(v);

        txtCliente.clear();
        txtProdutos.clear();
        txtQuantidade.clear();
        txtValor.clear();
        datePickerVenda.setValue(null);
    }

    @FXML
    private void onRemover(ActionEvent event) {
        Venda sel = tableVendas.getSelectionModel().getSelectedItem();
        if (sel != null) vendas.remove(sel);
    }

    @FXML
    private void onFinalizar(ActionEvent event) {
        Venda sel = tableVendas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setStatus("Concluída");
            tableVendas.refresh();
        }
    }
}
