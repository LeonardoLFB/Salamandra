package controller;

import java.io.IOException;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


import com.jfoenix.controls.JFXButton;

import database.ClienteDAO;
import database.ProdutoDAO;
import database.VendaDAO;
import model.Produto;
import model.Venda;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Comparator;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MenuController implements Initializable {

 
	@FXML private Label Menu;
    @FXML private Label MenuClose;
    @FXML private AnchorPane Slider;
    @FXML private JFXButton btClientes;
    @FXML private JFXButton btEstoque;
    @FXML private JFXButton btFornecedores;
    @FXML private JFXButton btProdutos;
    @FXML private JFXButton btUsuarios;
    @FXML private JFXButton btVendas;

    // Indicadores do painel inicial
    @FXML private Label lblQtdClientes;
    @FXML private Label lblQtdProdutos;
    @FXML private Label lblEstoqueBaixo;
    @FXML private Label lblQtdVendas;
    @FXML private Label lblFaturamento;
    @FXML private Label lblPendentes;

    @FXML private VBox boxUltimasVendas;
    @FXML private VBox boxEstoqueBaixo;
	
// fx:id="Slider"     (sidebar)

    private static final double SIDEBAR_WIDTH = 176; // ajuste se a largura for outra

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // começa fora da tela
        Slider.setTranslateX(-SIDEBAR_WIDTH);

        // ponteiros de clique (opcional)
        Menu.setCursor(Cursor.HAND);
        MenuClose.setCursor(Cursor.HAND);
       

        // estado inicial: mostra abrir, esconde fechar
        Menu.setVisible(true);
        MenuClose.setVisible(false);

        

        Menu.setOnMouseClicked(e -> openSidebar());
        MenuClose.setOnMouseClicked(e -> closeSidebar());

        carregarIndicadores();
    }

    /**
     * Preenche os cartões do painel inicial com os números do banco.
     * Falhas de conexão não podem derrubar o menu: nesse caso os
     * cartões apenas continuam mostrando "—".
     */
    private void carregarIndicadores() {
        try {
            List<Produto> produtos = new ProdutoDAO().getAll();
            List<Venda> vendas = new VendaDAO().getAll();

            int estoqueBaixo = 0;

            for (Produto p : produtos) {
                if (p.getQtdeEstoque() <= 10) {
                    estoqueBaixo++;
                }
            }

            int pendentes = 0;
            double faturamento = 0.0;

            for (Venda v : vendas) {
                if ("Pendente".equals(v.getStatus())) {
                    pendentes++;
                }

                if ("Concluída".equals(v.getStatus())) {
                    faturamento += v.getValorTotal();
                }
            }

            lblQtdClientes.setText(
                String.valueOf(new ClienteDAO().getAll().size())
            );

            lblQtdProdutos.setText(
                String.valueOf(produtos.size())
            );

            lblEstoqueBaixo.setText(
                String.valueOf(estoqueBaixo)
            );

            lblQtdVendas.setText(
                String.valueOf(vendas.size())
            );

            lblPendentes.setText(
                String.valueOf(pendentes)
            );

            lblFaturamento.setText(
                String.format("R$ %.2f", faturamento)
            );

            carregarUltimasVendas(vendas);
            carregarProdutosEstoqueBaixo(produtos);

        } catch (Exception e) {
            System.err.println(
                "Não foi possível carregar os indicadores: "
                + e.getMessage()
            );

            e.printStackTrace();
        }
    }
    
    private void carregarUltimasVendas(List<Venda> vendas) {

        boxUltimasVendas.getChildren().clear();

        if (vendas.isEmpty()) {

            Label vazio = new Label("Nenhuma venda encontrada.");
            vazio.getStyleClass().add("dashboard-empty");

            boxUltimasVendas.getChildren().add(vazio);

            return;
        }

        vendas.sort(
            Comparator.comparing(Venda::getData,
                Comparator.nullsLast(
                    Comparator.naturalOrder()
                )
            ).reversed()
        );

        int limite = Math.min(vendas.size(), 5);

        for (int i = 0; i < limite; i++) {

            Venda venda = vendas.get(i);

            VBox informacoes = new VBox(2);

            String cliente = venda.getNomeCliente();

            if (cliente == null || cliente.isBlank()) {
                cliente = "Cliente não informado";
            }

            Label titulo = new Label(
                "Venda #" + venda.getIdVenda()
                + " - " + cliente
            );

            titulo.getStyleClass().add(
                "dashboard-item-title"
            );

            Label detalhes = new Label(
                venda.getDataFormatada()
                + " • "
                + venda.getStatus()
            );

            detalhes.getStyleClass().add(
                "dashboard-item-sub"
            );

            informacoes.getChildren().addAll(
                titulo,
                detalhes
            );

            Label valor = new Label(
                venda.getValorFormatado()
            );

            valor.getStyleClass().add(
                "dashboard-item-title"
            );

            HBox linha = new HBox();

            linha.setSpacing(10);

            Region espaco = new Region();

            HBox.setHgrow(
                espaco,
                javafx.scene.layout.Priority.ALWAYS
            );

            linha.getChildren().addAll(
                informacoes,
                espaco,
                valor
            );

            linha.getStyleClass().add(
                "dashboard-item"
            );

            boxUltimasVendas
                .getChildren()
                .add(linha);
        }
    }
    
    private void carregarProdutosEstoqueBaixo(
            List<Produto> produtos) {

        boxEstoqueBaixo.getChildren().clear();

        List<Produto> produtosBaixos =
            produtos.stream()
                .filter(p -> p.getQtdeEstoque() <= 10)
                .sorted(
                    Comparator.comparingInt(
                        Produto::getQtdeEstoque
                    )
                )
                .limit(5)
                .toList();

        if (produtosBaixos.isEmpty()) {

            Label vazio = new Label(
                "Nenhum produto com estoque baixo."
            );

            vazio.getStyleClass().add(
                "dashboard-empty"
            );

            boxEstoqueBaixo
                .getChildren()
                .add(vazio);

            return;
        }

        for (Produto produto : produtosBaixos) {

            VBox informacoes = new VBox(2);

            Label nome = new Label(
                produto.getNome()
            );

            nome.getStyleClass().add(
                "dashboard-item-title"
            );

            Label codigo = new Label(
                "Código: " + produto.getCodigo()
            );

            codigo.getStyleClass().add(
                "dashboard-item-sub"
            );

            informacoes.getChildren().addAll(
                nome,
                codigo
            );

            Label estoque = new Label(
                produto.getQtdeEstoque()
                + " un."
            );

            estoque.getStyleClass().add(
                "dashboard-item-title"
            );

            HBox linha = new HBox();

            linha.setSpacing(10);

            Region espaco = new Region();

            HBox.setHgrow(
                espaco,
                javafx.scene.layout.Priority.ALWAYS
            );

            linha.getChildren().addAll(
                informacoes,
                espaco,
                estoque
            );

            linha.getStyleClass().add(
                "dashboard-item"
            );

            boxEstoqueBaixo
                .getChildren()
                .add(linha);
        }
    }
    
    private void openSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), Slider);
        slide.setToX(0);
        slide.setOnFinished((ActionEvent e) -> {
            Menu.setVisible(false);
            MenuClose.setVisible(true);
        });
        slide.play();
    }

    private void closeSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), Slider);
        slide.setToX(-SIDEBAR_WIDTH);
        slide.setOnFinished((ActionEvent e) -> {
            Menu.setVisible(true);
            MenuClose.setVisible(false);
        });
        slide.play();
    }
        
        public void OnBtProdutosClick (ActionEvent event) {
			try {
			           
			    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Produtos.fxml"));
			    Parent root = loader.load();
			    Stage stage = new Stage();
			    stage.setTitle("Nova Tela");
			    stage.setScene(new Scene(root));
			    stage.show();

			    } catch (IOException e) {
			            e.printStackTrace();
			    }
			    }
			
			public void OnBtEstoqueClick (ActionEvent event) {
				try {
				           
				    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Estoque.fxml"));
				    Parent root = loader.load();
				    Stage stage = new Stage();
				    stage.setTitle("Nova Tela");
				    stage.setScene(new Scene(root));
				    stage.show();

				    } catch (IOException e) {
				            e.printStackTrace();
				    }
			}
			public void OnBtFornecedoresClick (ActionEvent event) {
				try {
				           
				    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Fornecedores.fxml"));
				    Parent root = loader.load();
				    Stage stage = new Stage();
				    stage.setTitle("Nova Tela");
				    stage.setScene(new Scene(root));
				    stage.show();

				    } catch (IOException e) {
				            e.printStackTrace();
				    }
				    }
			public void OnBtClientesClick (ActionEvent event) {
				try {
				           
				    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Clientes.fxml"));
				    Parent root = loader.load();
				    Stage stage = new Stage();
				    stage.setTitle("Nova Tela");
				    stage.setScene(new Scene(root));
				    stage.show();

				    } catch (IOException e) {
				            e.printStackTrace();
				    }
				    }
			public void OnBtUsuariosClick (ActionEvent event) {
				try {
				           
				    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Usuarios.fxml"));
				    Parent root = loader.load();
				    Stage stage = new Stage();
				    stage.setTitle("Nova Tela");
				    stage.setScene(new Scene(root));
				    stage.show();

				    } catch (IOException e) {
				            e.printStackTrace();
				    }
				    }
			public void OnBtVendasClick (ActionEvent event) {
				try {
				           
				    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Vendas.fxml"));
				    Parent root = loader.load();
				    Stage stage = new Stage();
				    stage.setTitle("Nova Tela");
				    stage.setScene(new Scene(root));
				    stage.show();

				    } catch (IOException e) {
				            e.printStackTrace();
				    }
				    }
			
    }

