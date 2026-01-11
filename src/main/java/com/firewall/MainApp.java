package com.firewall;

import com.firewall.persistence.PacketRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application launcher.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));

        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setTitle("Firewall Security");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }

    @Override
    public void stop() {
        PacketRepository.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
