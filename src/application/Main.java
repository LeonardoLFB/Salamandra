package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            stage.setTitle("Login no Sistema");
            stage.setResizable(true);
            stage.sizeToScene();    // usa o tamanho do FXML
            // Carrega primeira tela (login)
            Parent root = FXMLLoader.load(getClass().getResource("/view/TelaLogin.fxml"));
            Scene scene = new Scene(root);

            
            stage.setScene(scene);
            stage.sizeToScene();
            
            //stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Troca de tela reaproveitando a mesma Scene. */
    public static void goTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource(fxmlPath));
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
