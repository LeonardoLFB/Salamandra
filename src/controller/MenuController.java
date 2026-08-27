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
                if (p.getQtdeEstoque() <= 10) estoqueBaixo++;
            }

            int pendentes = 0;
            double faturamento = 0.0;
            for (Venda v : vendas) {
                if ("Pendente".equals(v.getStatus())) pendentes++;
                if ("Concluída".equals(v.getStatus())) faturamento += v.getValorTotal();
            }

            lblQtdClientes.setText(String.valueOf(new ClienteDAO().getAll().size()));
            lblQtdProdutos.setText(String.valueOf(produtos.size()));
            lblEstoqueBaixo.setText(String.valueOf(estoqueBaixo));
            lblQtdVendas.setText(String.valueOf(vendas.size()));
            lblPendentes.setText(String.valueOf(pendentes));
            lblFaturamento.setText(String.format("R$ %.2f", faturamento));

        } catch (Exception e) {
            System.err.println("Não foi possível carregar os indicadores: " + e.getMessage());
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

