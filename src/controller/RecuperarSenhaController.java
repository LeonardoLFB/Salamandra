package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Window;

public class RecuperarSenhaController {

    @FXML
    private TextField campoEmailUsuario;

    @FXML
    private Button btnEnviar;

    @FXML
    private Hyperlink linkVoltar;

    @FXML
    private void initialize() {
        // Inicializações se necessário
    }

    @FXML
    private void handleEnviar() {
        String valor = campoEmailUsuario.getText();
        Window window = btnEnviar.getScene().getWindow();

        if (valor == null || valor.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, window, "Campo vazio", "Por favor, informe o e-mail ou nome de usuário.");
            return;
        }

        // Aqui você implementaria a lógica de recuperação:
        // - validar formato de e-mail (opcional)
        // - chamar um serviço REST que envie o e-mail de recuperação
        // - ou gerar token e enviar por SMTP
        //
        // Para fins de exemplo, mostramos diálogo de sucesso.

        boolean envioSimuladoComSucesso = true; // substituir pela chamada real
        if (envioSimuladoComSucesso) {
            showAlert(Alert.AlertType.INFORMATION, window, "Instruções enviadas",
                      "Se houver uma conta associada, você receberá instruções no e-mail informado.");
            campoEmailUsuario.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, window, "Erro", "Não foi possível enviar as instruções. Tente novamente.");
        }
    }

    @FXML
    private void handleVoltar() {
        // Implementar navegação de volta para a tela de login.
        // Exemplo simples: fechar a janela atual (se for diálogo) ou trocar de cena.
        // Aqui apenas mostraremos um alerta de exemplo.
        Window window = linkVoltar.getScene().getWindow();
        showAlert(Alert.AlertType.INFORMATION, window, "Voltar", "Voltar para a tela de login (implementar navegação).");
    }

    private void showAlert(Alert.AlertType type, Window owner, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}
