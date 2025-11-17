package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    public static Stage stage;
    private static final String GLOBAL_CSS = "/controller/styles.css";

    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            stage.setTitle("Login no Sistema");
            stage.setResizable(true);

            // carrega primeira tela (login)
            Parent root = FXMLLoader.load(getClass().getResource("/view/TelaLogin.fxml"));
            Scene scene = new Scene(root);
       

         // ✅ CSS global
         var url = getClass().getResource("/controller/styles.css");
         if (url == null) {
             System.err.println(">> CSS NÃO ENCONTRADO: /controller/styles.css");
         } else {
             scene.getStylesheets().add(url.toExternalForm());
         }
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Troca de tela reaproveitando a mesma Scene (mantém o CSS). */
    public static void goTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource(fxmlPath));
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                attachCss(scene);
                stage.setScene(scene);
            } else {
                scene.setRoot(root); // troca só o root
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Anexa (ou reanexa) o CSS global, evitando duplicar. */
    private static void attachCss(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.endsWith("styles.css"));
        scene.getStylesheets().add(
            Main.class.getResource(GLOBAL_CSS).toExternalForm()
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
