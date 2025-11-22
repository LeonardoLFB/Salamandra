package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

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

