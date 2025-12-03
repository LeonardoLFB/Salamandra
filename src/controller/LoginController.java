package controller;

import java.util.Optional;

import application.Main;
import database.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import model.Usuario;

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
    
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    
    // Método chamado automaticamente quando a tela carrega
    @FXML
    public void initialize() {
        // Garante que existe um usuário admin
        usuarioDAO.criarUsuarioPadrao();
        
        // Pressionar Enter no campo de login vai para o campo de senha
        tfLogin.setOnAction(e -> pfSenha.requestFocus());
        
        // Pressionar Enter no campo de senha faz o login
        pfSenha.setOnAction(e -> onClickLogar(null));
    }

    
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
        String login = tfLogin.getText();
        String senha = pfSenha.getText();
        
        if (login.isEmpty() || senha.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Campos Vazios");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, preencha login e senha!");
            alert.showAndWait();
            return;
        }
        
        try {
            Usuario usuario = usuarioDAO.autenticar(login, senha);
            
            if (usuario != null) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Login");
                alert.setHeaderText("Bem-vindo!");
                alert.setContentText("Login realizado com sucesso!\nUsuário: " + usuario.getNome());
                alert.showAndWait();
                
                Main.goTo("/view/MenuPrincipal.fxml");
                
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erro de Login");
                alert.setHeaderText("Credenciais Inválidas");
                alert.setContentText("Login ou senha incorretos!");
                alert.showAndWait();
                
                pfSenha.clear();
                tfLogin.requestFocus();
            }
            
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao conectar ao banco de dados");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}