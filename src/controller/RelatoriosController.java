package controller;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import database.VendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.ProdutoMaisVendido;
import model.Venda;

import java.util.Comparator;

import database.ProdutoDAO;
import model.Produto;

import model.ClienteMaisComprou;

public class RelatoriosController implements Initializable {

    // ============================================================
    // FILTROS
    // ============================================================

    @FXML
    private DatePicker dpDataInicial;

    @FXML
    private DatePicker dpDataFinal;

    // ============================================================
    // RELATÓRIO DE VENDAS
    // ============================================================

    @FXML
    private Label lblQuantidadeVendas;

    @FXML
    private Label lblFaturamento;
    
    @FXML
    private Label lblFaturamentoPeriodo;

    @FXML
    private Label lblVendasConcluidas;

    @FXML
    private Label lblTicketMedio;

    @FXML
    private TableView<Venda> tabelaVendas;

    @FXML
    private TableColumn<Venda, Integer> colId;

    @FXML
    private TableColumn<Venda, String> colData;

    @FXML
    private TableColumn<Venda, String> colCliente;

    @FXML
    private TableColumn<Venda, String> colValor;

    @FXML
    private TableColumn<Venda, String> colStatus;

    // ============================================================
    // RELATÓRIO DE PRODUTOS MAIS VENDIDOS
    // ============================================================

    @FXML
    private TableView<ProdutoMaisVendido> tabelaProdutosMaisVendidos;

    @FXML
    private TableColumn<ProdutoMaisVendido, Integer> colProdutoId;

    @FXML
    private TableColumn<ProdutoMaisVendido, String> colProdutoNome;

    @FXML
    private TableColumn<ProdutoMaisVendido, Integer> colQuantidadeVendida;

    @FXML
    private TableColumn<ProdutoMaisVendido, String> colProdutoValorTotal;
    
    // ============================================================
    // RELATÓRIO DE ESTOQUE BAIXO
    // ============================================================

    @FXML
    private TableView<Produto> tabelaEstoqueBaixo;

    @FXML
    private TableColumn<Produto, Integer> colEstoqueId;

    @FXML
    private TableColumn<Produto, String> colEstoqueProduto;

    @FXML
    private TableColumn<Produto, Integer> colEstoqueQuantidade;

    @FXML
    private TableColumn<Produto, String> colEstoqueSituacao;

    @FXML
    private Label lblProdutosEstoqueBaixo;

    @FXML
    private Label lblProdutosSemEstoque;

 // ============================================================
 // RELATÓRIO DE CLIENTES QUE MAIS COMPRARAM
 // ============================================================

	 @FXML
	 private TableView<ClienteMaisComprou> tabelaClientesMaisCompraram;
	
	 @FXML
	 private TableColumn<ClienteMaisComprou, Integer> colClienteId;
	
	 @FXML
	 private TableColumn<ClienteMaisComprou, String> colClienteNome;
	
	 @FXML
	 private TableColumn<ClienteMaisComprou, Integer> colClienteQuantidadeCompras;
	
	 @FXML
	 private TableColumn<ClienteMaisComprou, String> colClienteValorTotal;
 
    // ============================================================
    // DAO
    // ============================================================

    private VendaDAO vendaDAO;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources) {

        vendaDAO = new VendaDAO();

        configurarTabelaVendas();

        configurarTabelaProdutosMaisVendidos();
        
        configurarTabelaEstoqueBaixo();
        
        configurarTabelaClientesMaisCompraram();

        // Começa mostrando o mês atual
        LocalDate hoje = LocalDate.now();

        dpDataInicial.setValue(
                hoje.withDayOfMonth(1)
        );

        dpDataFinal.setValue(
                hoje
        );

        buscarRelatorios();
        
        buscarEstoqueBaixo();
    }

    // ============================================================
    // CONFIGURAÇÃO DA TABELA DE VENDAS
    // ============================================================

    private void configurarTabelaVendas() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idVenda"
                )
        );

        colData.setCellValueFactory(
                new PropertyValueFactory<>(
                        "dataFormatada"
                )
        );

        colCliente.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nomeCliente"
                )
        );

        colValor.setCellValueFactory(
                new PropertyValueFactory<>(
                        "valorFormatado"
                )
        );

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>(
                        "status"
                )
        );
    }

    // ============================================================
    // CONFIGURAÇÃO DA TABELA DE PRODUTOS MAIS VENDIDOS
    // ============================================================

    private void configurarTabelaProdutosMaisVendidos() {

        colProdutoId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idProduto"
                )
        );

        colProdutoNome.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nomeProduto"
                )
        );

        colQuantidadeVendida.setCellValueFactory(
                new PropertyValueFactory<>(
                        "quantidadeVendida"
                )
        );

        colProdutoValorTotal.setCellValueFactory(
                new PropertyValueFactory<>(
                        "valorFormatado"
                )
        );
    }

    // ============================================================
    // BUSCAR RELATÓRIOS
    // ============================================================

    @FXML
    private void buscarRelatorios() {

        LocalDate dataInicial =
                dpDataInicial.getValue();

        LocalDate dataFinal =
                dpDataFinal.getValue();

        // Validação das datas
        if (dataInicial == null
                || dataFinal == null) {

            mostrarAviso(
                    "Informe a data inicial e a data final."
            );

            return;
        }

        if (dataInicial.isAfter(dataFinal)) {

            mostrarAviso(
                    "A data inicial não pode ser maior "
                    + "que a data final."
            );

            return;
        }

        LocalDateTime inicio =
                dataInicial.atStartOfDay();

        LocalDateTime fim =
                dataFinal.atTime(
                        23,
                        59,
                        59
                );

        buscarVendas(
                inicio,
                fim
        );

        buscarProdutosMaisVendidos(
                inicio,
                fim
        );
        
        buscarClientesMaisCompraram(
                inicio,
                fim
        );
    }

    // ============================================================
    // VENDAS POR PERÍODO
    // ============================================================

    private void buscarVendas(
            LocalDateTime inicio,
            LocalDateTime fim) {

        List<Venda> vendas =
                vendaDAO.buscarPorPeriodo(
                        inicio,
                        fim
                );

        ObservableList<Venda> dados =
                FXCollections.observableArrayList(
                        vendas
                );

        tabelaVendas.setItems(
                dados
        );

        atualizarResumo(
                vendas
                
        );
        
        atualizarFaturamento(
                vendas
        );
    }

    // ============================================================
    // PRODUTOS MAIS VENDIDOS
    // ============================================================

    private void buscarProdutosMaisVendidos(
            LocalDateTime inicio,
            LocalDateTime fim) {

        List<ProdutoMaisVendido> produtos =
                vendaDAO.buscarProdutosMaisVendidos(
                        inicio,
                        fim
                );

        ObservableList<ProdutoMaisVendido> dados =
                FXCollections.observableArrayList(
                        produtos
                );

        tabelaProdutosMaisVendidos.setItems(
                dados
        );
    }

    // ============================================================
    // RESUMO DE VENDAS
    // ============================================================

    private void atualizarResumo(
            List<Venda> vendas) {

        int quantidade = 0;

        double faturamento = 0;

        for (Venda venda : vendas) {

            // Vendas canceladas não entram no resumo
            if (!"Cancelada".equalsIgnoreCase(
                    venda.getStatus())) {

                quantidade++;

                faturamento +=
                        venda.getValorTotal();
            }
        }

        lblQuantidadeVendas.setText(
                String.valueOf(
                        quantidade
                )
        );

        NumberFormat moeda =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "pt",
                                "BR"
                        )
                );

        lblFaturamento.setText(
                moeda.format(
                        faturamento
                )
        );
    }
    
 // ============================================================
 // FATURAMENTO POR PERÍODO
 // ============================================================

 private void atualizarFaturamento(
         List<Venda> vendas) {

     int vendasConcluidas = 0;
     double faturamento = 0.0;

     for (Venda venda : vendas) {

         if ("Concluída".equalsIgnoreCase(
                 venda.getStatus())) {

             vendasConcluidas++;

             faturamento +=
                     venda.getValorTotal();
         }
     }

     double ticketMedio = 0.0;

     if (vendasConcluidas > 0) {

         ticketMedio =
                 faturamento / vendasConcluidas;
     }

     NumberFormat moeda =
             NumberFormat.getCurrencyInstance(
                     new Locale(
                             "pt",
                             "BR"
                     )
             );

     lblFaturamentoPeriodo.setText(
             moeda.format(faturamento)
     );

     lblVendasConcluidas.setText(
             String.valueOf(vendasConcluidas)
     );

     lblTicketMedio.setText(
             moeda.format(ticketMedio)
     );
 }

    // ============================================================
    // ALERTA
    // ============================================================

    private void mostrarAviso(
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Relatórios"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }
    

 
//============================================================
//CONFIGURAÇÃO DA TABELA DE ESTOQUE BAIXO
//============================================================

private void configurarTabelaEstoqueBaixo() {

  colEstoqueId.setCellValueFactory(
          new PropertyValueFactory<>(
                  "idProduto"
          )
  );

  colEstoqueProduto.setCellValueFactory(
          new PropertyValueFactory<>(
                  "nome"
          )
  );

  colEstoqueQuantidade.setCellValueFactory(
          new PropertyValueFactory<>(
                  "qtdeEstoque"
          )
  );

  colEstoqueSituacao.setCellValueFactory(
          dados -> {

              int quantidade =
                      dados.getValue()
                           .getQtdeEstoque();

              String situacao;

              if (quantidade <= 0) {

                  situacao = "Sem estoque";

              } else if (quantidade <= 5) {

                  situacao = "Crítico";

              } else {

                  situacao = "Baixo";
              }

              return new javafx.beans.property.SimpleStringProperty(
                      situacao
              );
          }
  );
}

//============================================================
//ESTOQUE BAIXO
//============================================================

private void buscarEstoqueBaixo() {

 try {

     List<Produto> produtos =
             new ProdutoDAO().getAll();

     List<Produto> estoqueBaixo =
             produtos.stream()
                     .filter(
                             produto ->
                                     produto.getQtdeEstoque() <= 10
                     )
                     .sorted(
                             Comparator.comparingInt(
                                     Produto::getQtdeEstoque
                             )
                     )
                     .toList();

     tabelaEstoqueBaixo.setItems(
             FXCollections.observableArrayList(
                     estoqueBaixo
             )
     );

     int semEstoque = 0;

     for (Produto produto : estoqueBaixo) {

         if (produto.getQtdeEstoque() <= 0) {
             semEstoque++;
         }
     }

     lblProdutosEstoqueBaixo.setText(
             String.valueOf(
                     estoqueBaixo.size()
             )
     );

     lblProdutosSemEstoque.setText(
             String.valueOf(
                     semEstoque
             )
     );

 } catch (Exception e) {

     System.err.println(
             "Erro ao carregar relatório de estoque baixo: "
             + e.getMessage()
     );

     e.printStackTrace();
 }
}

//============================================================
//CONFIGURAÇÃO DA TABELA DE CLIENTES QUE MAIS COMPRARAM
//============================================================

private void configurarTabelaClientesMaisCompraram() {

 colClienteId.setCellValueFactory(
         new PropertyValueFactory<>(
                 "idCliente"
         )
 );

 colClienteNome.setCellValueFactory(
         new PropertyValueFactory<>(
                 "nomeCliente"
         )
 );

 colClienteQuantidadeCompras.setCellValueFactory(
         new PropertyValueFactory<>(
                 "quantidadeCompras"
         )
 );

 colClienteValorTotal.setCellValueFactory(
         new PropertyValueFactory<>(
                 "valorFormatado"
         )
 );
}

//============================================================
//CLIENTES QUE MAIS COMPRARAM
//============================================================

private void buscarClientesMaisCompraram(
     LocalDateTime inicio,
     LocalDateTime fim) {

 List<ClienteMaisComprou> clientes =
         vendaDAO.buscarClientesQueMaisCompraram(
                 inicio,
                 fim
         );

 ObservableList<ClienteMaisComprou> dados =
         FXCollections.observableArrayList(
                 clientes
         );

 tabelaClientesMaisCompraram.setItems(
         dados
 );
}

}