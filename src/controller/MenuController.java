package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MenuController implements Initializable {

    @FXML private Label Menu;         // ícone ☰ abrir sidebar
    @FXML private Label MenuClose;    // ícone ✕ fechar sidebar
    @FXML private AnchorPane Slider;

    @FXML private Button btProdutos;
    @FXML private Button btEstoque;

    private static final double SIDEBAR_WIDTH = 176;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Slider.setTranslateX(-SIDEBAR_WIDTH);

        Menu.setCursor(Cursor.HAND);
        MenuClose.setCursor(Cursor.HAND);

        Menu.setVisible(true);
        MenuClose.setVisible(false);

        Menu.setOnMouseClicked(e -> openSidebar());
        MenuClose.setOnMouseClicked(e -> closeSidebar());
    }

    private void openSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), Slider);
        slide.setToX(0);
        slide.setOnFinished(e -> {
            Menu.setVisible(false);
            MenuClose.setVisible(true);
        });
        slide.play();
    }

    private void closeSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), Slider);
        slide.setToX(-SIDEBAR_WIDTH);
        slide.setOnFinished(e -> {
            Menu.setVisible(true);
            MenuClose.setVisible(false);
        });
        slide.play();
    }

    // Método utilitário para abrir uma nova view com barra de título (X)
    private void openView(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);

            // garante barra com X
            stage.initStyle(StageStyle.DECORATED);

            // define janela pai (melhor comportamento de foco)
            if (event != null && event.getSource() instanceof Node) {
                Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.initOwner(owner);
            }

            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // BOTÕES DO MENU – todos usando openView()

    public void OnBtProdutosClick(ActionEvent event) {
        openView("/view/Produtos.fxml", "Produtos", event);
    }

    public void OnBtEstoqueClick(ActionEvent event) {
        openView("/view/Estoque.fxml", "Estoque", event);
    }

    public void OnBtFornecedoresClick(ActionEvent event) {
        openView("/view/Fornecedores.fxml", "Fornecedores", event);
    }

    public void OnBtClientesClick(ActionEvent event) {
        openView("/view/Clientes.fxml", "Clientes", event);
    }

    public void OnBtUsuariosClick(ActionEvent event) {
        openView("/view/Usuarios.fxml", "Usuários", event);
    }

    public void OnBtVendasClick(ActionEvent event) {
        openView("/view/Vendas.fxml", "Vendas", event);
    }
}
