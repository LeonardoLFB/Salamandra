package controller;

import javafx.scene.control.MenuItem;
import java.io.IOException;
import java.util.Optional;
import javax.swing.JOptionPane;
import application.Main; // ✅ IMPORTANTE

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class LoginController {

    @FXML
    private Button btCancelar;
    @FXML
    private Button btLogar;
    @FXML
    private PasswordField pfSenha;
    @FXML
    private TextField tfLogin;
    @FXML
    private Hyperlink btEsqueciSenha;


    @FXML
    void onClickCancelar(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Deseja realmente sair?");
        alert.setContentText("Clique em OK para confirmar.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    @FXML
    void onClickLogar(ActionEvent event) {
        if (tfLogin.getText().isEmpty() || pfSenha.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha os dois campos!");
        } 
        else if (tfLogin.getText().equals("fatec") && pfSenha.getText().equals("fatec")) {

            // Troca de tela 
            Main.goTo("/view/MenuPrincipal.fxml");

        } 
        else {
            JOptionPane.showMessageDialog(null, "Credenciais inválidas");
            tfLogin.requestFocus();
        }
        }
        
        @FXML
        void onClickEsqueciSenha(ActionEvent event) {
        	try {
		           
		        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Recuperação de Senha.fxml"));
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

