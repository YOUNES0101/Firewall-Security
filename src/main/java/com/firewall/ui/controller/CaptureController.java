package com.firewall.ui.controller;

import com.firewall.capture.PacketCapture;
import com.firewall.domain.Packet;
import com.firewall.domain.ProtocolType;
import com.firewall.logic.FirewallEngine;
import com.firewall.logic.rules.*;
import com.firewall.persistence.PacketRepository;
import com.firewall.persistence.entity.CapturedPacket;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.pcap4j.core.PcapNetworkInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for packet capture and history page.
 */
public class CaptureController {

    // Navigation
    @FXML
    private StackPane contentStack;
    @FXML
    private VBox captureView;
    @FXML
    private VBox historyView;
    @FXML
    private HBox captureNavItem;
    @FXML
    private HBox historyNavItem;

    // Header & Status
    @FXML
    private Label interfaceLabel;
    @FXML
    private Label sessionLabel;
    @FXML
    private Label dbStatusLabel;
    @FXML
    private Circle dbIndicator;
    @FXML
    private Label ruleCountLabel;
    @FXML
    private Label packetCountLabel;

    // Rule inputs
    @FXML
    private TextField ipBlockField;
    @FXML
    private TextField ipRangeField;
    @FXML
    private TextField portBlockField;
    @FXML
    private TextField portStartField;
    @FXML
    private TextField portEndField;
    @FXML
    private ComboBox<String> protocolCombo;
    @FXML
    private TextField payloadSizeField;
    @FXML
    private TextField maxPacketsField;
    @FXML
    private ListView<String> rulesListView;

    // Capture controls
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private ProgressIndicator captureProgress;
    @FXML
    private Label captureStatusLabel;

    // Capture table
    @FXML
    private TableView<PacketRow> packetsTable;
    @FXML
    private TableColumn<PacketRow, String> timeColumn;
    @FXML
    private TableColumn<PacketRow, String> srcIpColumn;
    @FXML
    private TableColumn<PacketRow, String> srcPortColumn;
    @FXML
    private TableColumn<PacketRow, String> dstIpColumn;
    @FXML
    private TableColumn<PacketRow, String> dstPortColumn;
    @FXML
    private TableColumn<PacketRow, String> protocolColumn;
    @FXML
    private TableColumn<PacketRow, String> statusColumn;

    // History table
    @FXML
    private TableView<HistoryRow> historyTable;
    @FXML
    private TableColumn<HistoryRow, String> hIdColumn;
    @FXML
    private TableColumn<HistoryRow, String> hTimeColumn;
    @FXML
    private TableColumn<HistoryRow, String> hSrcIpColumn;
    @FXML
    private TableColumn<HistoryRow, String> hSrcPortColumn;
    @FXML
    private TableColumn<HistoryRow, String> hDstIpColumn;
    @FXML
    private TableColumn<HistoryRow, String> hDstPortColumn;
    @FXML
    private TableColumn<HistoryRow, String> hProtocolColumn;
    @FXML
    private TableColumn<HistoryRow, String> hRuleColumn;
    @FXML
    private TableColumn<HistoryRow, String> hSessionColumn;
    @FXML
    private Label historyCountLabel;

    private PcapNetworkInterface networkInterface;
    private int interfaceIndex;
    private String sessionId;
    private final FirewallEngine engine = new FirewallEngine();
    private final PacketCapture capture = new PacketCapture();
    private final PacketRepository repository = new PacketRepository();
    private final ObservableList<String> rulesList = FXCollections.observableArrayList();
    private final ObservableList<PacketRow> packetRows = FXCollections.observableArrayList();
    private final ObservableList<HistoryRow> historyRows = FXCollections.observableArrayList();
    private final List<CapturedPacket> capturedPackets = new ArrayList<>();
    private final AtomicInteger packetCount = new AtomicInteger(0);
    private volatile boolean isCapturing = false;
    private boolean dbConnected = false;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @FXML
    public void initialize() {
        rulesListView.setItems(rulesList);
        packetsTable.setItems(packetRows);
        historyTable.setItems(historyRows);

        // Setup protocol combo
        protocolCombo.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP"));
        protocolCombo.setValue("TCP");

        setupCaptureTable();
        setupHistoryTable();

        sessionId = UUID.randomUUID().toString().substring(0, 8);
        sessionLabel.setText(sessionId);

        checkDatabase();
    }

    private void setupCaptureTable() {
        timeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time()));
        srcIpColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().srcIp()));
        srcPortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().srcPort()));
        dstIpColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dstIp()));
        dstPortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dstPort()));
        protocolColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().protocol()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("BLOCKED".equals(item)) {
                        setStyle("-fx-text-fill: #d13438; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #107c10; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupHistoryTable() {
        hIdColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id()));
        hTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time()));
        hSrcIpColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().srcIp()));
        hSrcPortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().srcPort()));
        hDstIpColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dstIp()));
        hDstPortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dstPort()));
        hProtocolColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().protocol()));
        hRuleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().rule()));
        hSessionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().session()));
    }

    public void setNetworkInterface(PcapNetworkInterface nif, int index) {
        this.networkInterface = nif;
        this.interfaceIndex = index;
        String name = nif.getDescription() != null ? nif.getDescription() : nif.getName();
        interfaceLabel.setText(name);
    }

    private void checkDatabase() {
        new Thread(() -> {
            try {
                PacketRepository.initialize();
                dbConnected = true;
                Platform.runLater(() -> {
                    dbStatusLabel.setText("Connected");
                    dbIndicator.setStyle("-fx-fill: #107c10;");
                });
            } catch (Exception e) {
                dbConnected = false;
                Platform.runLater(() -> {
                    dbStatusLabel.setText("Offline");
                    dbIndicator.setStyle("-fx-fill: #d13438;");
                });
            }
        }).start();
    }

    // Tab switching
    @FXML
    public void showCaptureTab() {
        captureView.setVisible(true);
        historyView.setVisible(false);
        captureNavItem.setStyle(
                "-fx-background-color: #e5f3ff; -fx-background-radius: 6; -fx-padding: 10 12; -fx-cursor: hand;");
        historyNavItem.setStyle("-fx-background-radius: 6; -fx-padding: 10 12; -fx-cursor: hand;");
    }

    @FXML
    public void showHistoryTab() {
        captureView.setVisible(false);
        historyView.setVisible(true);
        historyNavItem.setStyle(
                "-fx-background-color: #e5f3ff; -fx-background-radius: 6; -fx-padding: 10 12; -fx-cursor: hand;");
        captureNavItem.setStyle("-fx-background-radius: 6; -fx-padding: 10 12; -fx-cursor: hand;");
        onRefreshHistory();
    }

    // ==================== RULE HANDLERS ====================

    @FXML
    public void onAddIpRule() {
        String ip = ipBlockField.getText().trim();
        if (ip.isEmpty())
            return;

        engine.addRule(new IPBlockRule("Block " + ip, 100, ip));
        rulesList.add("🚫 IP: " + ip);
        ipBlockField.clear();
        updateRuleCount();
    }

    @FXML
    public void onAddIpRangeRule() {
        String cidr = ipRangeField.getText().trim();
        if (cidr.isEmpty() || !cidr.contains("/")) {
            captureStatusLabel.setText("Invalid CIDR format (e.g., 192.168.1.0/24)");
            return;
        }

        try {
            engine.addRule(new IPRangeBlockRule("Block " + cidr, 95, cidr));
            rulesList.add("🚫 Range: " + cidr);
            ipRangeField.clear();
            updateRuleCount();
        } catch (Exception e) {
            captureStatusLabel.setText("Invalid CIDR: " + e.getMessage());
        }
    }

    @FXML
    public void onAddPortRule() {
        String portStr = portBlockField.getText().trim();
        if (portStr.isEmpty())
            return;

        try {
            int port = Integer.parseInt(portStr);
            engine.addRule(new PortBlockRule("Block Port " + port, 90, port));
            rulesList.add("🚫 Port: " + port);
            portBlockField.clear();
            updateRuleCount();
        } catch (NumberFormatException e) {
            captureStatusLabel.setText("Invalid port number");
        }
    }

    @FXML
    public void onAddPortRangeRule() {
        String startStr = portStartField.getText().trim();
        String endStr = portEndField.getText().trim();
        if (startStr.isEmpty() || endStr.isEmpty())
            return;

        try {
            int start = Integer.parseInt(startStr);
            int end = Integer.parseInt(endStr);
            engine.addRule(new PortBlockRule("Block Ports " + start + "-" + end, 85, start, end,
                    PortBlockRule.PortMatchMode.BOTH));
            rulesList.add("🚫 Ports: " + start + "-" + end);
            portStartField.clear();
            portEndField.clear();
            updateRuleCount();
        } catch (NumberFormatException e) {
            captureStatusLabel.setText("Invalid port range");
        }
    }

    @FXML
    public void onAddProtocolRule() {
        String protocol = protocolCombo.getValue();
        if (protocol == null)
            return;

        ProtocolType type = ProtocolType.fromString(protocol);
        engine.addRule(new ProtocolBlockRule("Block " + protocol, 80, type));
        rulesList.add("🚫 Protocol: " + protocol);
        updateRuleCount();
    }

    @FXML
    public void onAddPayloadRule() {
        String sizeStr = payloadSizeField.getText().trim();
        if (sizeStr.isEmpty())
            return;

        try {
            int size = Integer.parseInt(sizeStr);
            engine.addRule(PayloadSizeRule.blockLargerThan("Block > " + size + " bytes", 75, size));
            rulesList.add("🚫 Size > " + size + " bytes");
            payloadSizeField.clear();
            updateRuleCount();
        } catch (NumberFormatException e) {
            captureStatusLabel.setText("Invalid size");
        }
    }

    @FXML
    public void onClearRules() {
        engine.clearRules();
        rulesList.clear();
        updateRuleCount();
    }

    private void updateRuleCount() {
        ruleCountLabel.setText(String.valueOf(engine.getRules().size()));
    }

    // ==================== CAPTURE ====================

    @FXML
    public void onStartCapture() {
        if (isCapturing)
            return;

        int maxPackets = 100;
        try {
            maxPackets = Math.min(Integer.parseInt(maxPacketsField.getText().trim()), 100);
            if (maxPackets <= 0)
                maxPackets = 100;
        } catch (NumberFormatException ignored) {
        }

        final int limit = maxPackets;

        isCapturing = true;
        startButton.setDisable(true);
        stopButton.setDisable(false);
        captureProgress.setVisible(true);
        captureStatusLabel.setText("Capturing...");
        packetCount.set(0);
        capturedPackets.clear();

        String ifaceName = networkInterface.getDescription() != null
                ? networkInterface.getDescription()
                : networkInterface.getName();

        new Thread(() -> {
            capture.startCapture(interfaceIndex, packet -> {
                if (!isCapturing)
                    return;

                boolean blocked = engine.shouldBlock(packet);
                FilterRule matchedRule = blocked ? engine.getMatchingRule(packet) : null;
                String status = blocked ? "BLOCKED" : "ALLOWED";
                String ruleName = matchedRule != null ? matchedRule.getDescription() : null;

                PacketRow row = new PacketRow(
                        TIME_FORMAT.format(packet.getTimestamp()),
                        packet.getSourceIP(),
                        packet.getSourcePort() != null ? packet.getSourcePort().toString() : "-",
                        packet.getDestIP(),
                        packet.getDestPort() != null ? packet.getDestPort().toString() : "-",
                        packet.getProtocol().name(),
                        status);

                CapturedPacket entity = CapturedPacket.builder()
                        .sourceIp(packet.getSourceIP())
                        .destIp(packet.getDestIP())
                        .sourcePort(packet.getSourcePort())
                        .destPort(packet.getDestPort())
                        .protocol(packet.getProtocol())
                        .payloadSize(packet.getPayloadSize())
                        .capturedAt(packet.getTimestamp())
                        .matchedRule(ruleName)
                        .sessionId(sessionId)
                        .interfaceName(ifaceName)
                        .build();

                synchronized (capturedPackets) {
                    capturedPackets.add(entity);
                }

                int count = packetCount.incrementAndGet();

                Platform.runLater(() -> {
                    packetRows.add(0, row);
                    if (packetRows.size() > 500)
                        packetRows.remove(packetRows.size() - 1);
                    packetCountLabel.setText(String.valueOf(count));
                });

                if (count >= limit)
                    stopCaptureInternal();

            }, limit);
        }).start();
    }

    @FXML
    public void onStopCapture() {
        stopCaptureInternal();
    }

    private void stopCaptureInternal() {
        isCapturing = false;
        capture.stopCapture();

        Platform.runLater(() -> {
            startButton.setDisable(false);
            stopButton.setDisable(true);
            captureProgress.setVisible(false);
            captureStatusLabel.setText("Capture complete");
        });

        if (dbConnected && !capturedPackets.isEmpty()) {
            new Thread(() -> {
                try {
                    synchronized (capturedPackets) {
                        repository.saveAll(new ArrayList<>(capturedPackets));
                    }
                    Platform.runLater(() -> captureStatusLabel.setText("Saved " + capturedPackets.size() + " packets"));
                } catch (Exception e) {
                    Platform.runLater(() -> captureStatusLabel.setText("Save failed"));
                }
            }).start();
        }
    }

    // ==================== HISTORY ====================

    @FXML
    public void onRefreshHistory() {
        if (!dbConnected) {
            historyCountLabel.setText("Database offline");
            return;
        }

        new Thread(() -> {
            try {
                List<CapturedPacket> packets = repository.findRecent(200);
                Platform.runLater(() -> {
                    historyRows.clear();
                    for (CapturedPacket p : packets) {
                        historyRows.add(new HistoryRow(
                                p.getId().toString(),
                                DATETIME_FORMAT.format(p.getCapturedAt()),
                                p.getSourceIp(),
                                p.getSourcePort() != null ? p.getSourcePort().toString() : "-",
                                p.getDestIp(),
                                p.getDestPort() != null ? p.getDestPort().toString() : "-",
                                p.getProtocol() != null ? p.getProtocol().name() : "-",
                                p.getMatchedRule() != null ? p.getMatchedRule() : "-",
                                p.getSessionId()));
                    }
                    historyCountLabel.setText(packets.size() + " records");
                });
            } catch (Exception e) {
                Platform.runLater(() -> historyCountLabel.setText("Error loading"));
            }
        }).start();
    }

    @FXML
    public void onDeleteAllHistory() {
        historyRows.clear();
        historyCountLabel.setText("0 records");
    }

    // ==================== EXPORT & NAVIGATION ====================

    @FXML
    public void onExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName("packets_" + sessionId + ".csv");

        File file = chooser.showSaveDialog(packetsTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
                w.println("Time,Source IP,Src Port,Dest IP,Dst Port,Protocol,Status");
                for (PacketRow r : packetRows) {
                    w.printf("%s,%s,%s,%s,%s,%s,%s%n",
                            r.time(), r.srcIp(), r.srcPort(), r.dstIp(), r.dstPort(), r.protocol(), r.status());
                }
                captureStatusLabel.setText("Exported to CSV");
            } catch (Exception e) {
                captureStatusLabel.setText("Export failed");
            }
        }
    }

    @FXML
    public void onClearTable() {
        packetRows.clear();
        packetCount.set(0);
        packetCountLabel.setText("0");
    }

    @FXML
    public void onBack() {
        if (isCapturing)
            stopCaptureInternal();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            Stage stage = (Stage) interfaceLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Data records
    public record PacketRow(String time, String srcIp, String srcPort,
            String dstIp, String dstPort, String protocol, String status) {
    }

    public record HistoryRow(String id, String time, String srcIp, String srcPort,
            String dstIp, String dstPort, String protocol, String rule, String session) {
    }
}
