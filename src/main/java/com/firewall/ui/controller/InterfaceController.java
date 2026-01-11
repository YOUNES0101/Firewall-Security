package com.firewall.ui.controller;

import com.firewall.capture.PacketCapture;
import com.firewall.persistence.PacketRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.pcap4j.core.PcapNetworkInterface;

import java.util.List;

/**
 * Controller for interface selection page.
 */
public class InterfaceController {

    @FXML
    private ListView<String> interfaceListView;
    @FXML
    private Button scanButton;
    @FXML
    private Button selectButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label statusLabel;
    @FXML
    private Label dbStatusLabel;
    @FXML
    private Label countLabel;
    @FXML
    private Circle dbIndicator;

    private final ObservableList<String> interfaces = FXCollections.observableArrayList();
    private List<PcapNetworkInterface> networkInterfaces;

    @FXML
    public void initialize() {
        interfaceListView.setItems(interfaces);

        // Custom cell factory for better styling
        interfaceListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 12 16; -fx-font-size: 13px;");
                }
            }
        });

        // Enable select button when item is selected
        interfaceListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectButton.setDisable(newVal == null);
            if (newVal != null) {
                selectButton.setStyle(
                        "-fx-background-color: #107c10; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });

        // Check database connection
        checkDatabaseConnection();
    }

    private void checkDatabaseConnection() {
        new Thread(() -> {
            try {
                PacketRepository.initialize();
                Platform.runLater(() -> {
                    dbStatusLabel.setText("Database connected");
                    dbIndicator.setStyle("-fx-fill: #107c10;");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    dbStatusLabel.setText("Database offline");
                    dbIndicator.setStyle("-fx-fill: #d13438;");
                });
            }
        }).start();
    }

    @FXML
    public void onScanInterfaces() {
        progressIndicator.setVisible(true);
        scanButton.setDisable(true);
        statusLabel.setText("Scanning network adapters...");
        interfaces.clear();
        countLabel.setText("Scanning...");

        new Thread(() -> {
            try {
                networkInterfaces = PacketCapture.getNetworkInterfaces();

                Platform.runLater(() -> {
                    for (int i = 0; i < networkInterfaces.size(); i++) {
                        PcapNetworkInterface nif = networkInterfaces.get(i);
                        String name = nif.getDescription() != null ? nif.getDescription() : nif.getName();
                        interfaces.add(name);
                    }

                    progressIndicator.setVisible(false);
                    scanButton.setDisable(false);
                    countLabel.setText(interfaces.size() + " found");
                    statusLabel.setText("Scan complete. Select an interface to continue.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    scanButton.setDisable(false);
                    countLabel.setText("0 found");

                    String msg = e.getMessage();
                    if (msg != null && (msg.contains("Npcap") || msg.contains("native"))) {
                        statusLabel.setText("Error: Npcap not installed or not running as Administrator");
                        interfaces.add("⚠ Install Npcap and run as Administrator");
                    } else {
                        statusLabel.setText("Error: " + msg);
                    }
                });
            }
        }).start();
    }

    @FXML
    public void onSelectInterface() {
        int selectedIndex = interfaceListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || networkInterfaces == null || selectedIndex >= networkInterfaces.size()) {
            return;
        }

        try {
            PcapNetworkInterface selectedInterface = networkInterfaces.get(selectedIndex);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CaptureView.fxml"));
            Parent root = loader.load();

            CaptureController controller = loader.getController();
            controller.setNetworkInterface(selectedInterface, selectedIndex);

            Stage stage = (Stage) selectButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 650);
            stage.setScene(scene);

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
