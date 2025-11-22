package controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DashboardController {

    @FXML
    private ImageView logoImageView;

    // Path to the logo image you uploaded.
    // Observe: Scene Builder / JavaFX accepts file: URIs for local files.
    private static final String LOGO_PATH = "file:/mnt/data/1e47ab63-5f2d-4659-b798-742a2c676fea.png";

    @FXML
    public void initialize() {
        // Load logo (if exists). Keeps aspect ratio.
        try {
            Image img = new Image(LOGO_PATH, true);
            logoImageView.setImage(img);
        } catch (Exception e) {
            // if fails, ignore silently; Scene Builder will still show layout
            System.err.println("Logo load failed: " + e.getMessage());
        }
    }

    // TODO: adicionar métodos para atualizar números dos cards a partir do modelo / BD
}
