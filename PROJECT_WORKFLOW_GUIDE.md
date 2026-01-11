# 🛡️ Firewall Security Application - Complete Workflow Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Application Startup Flow](#application-startup-flow)
4. [Page Navigation Flow](#page-navigation-flow)
5. [Detailed Component Breakdown](#detailed-component-breakdown)
6. [Complete Workflow Scenario](#complete-workflow-scenario)
7. [Data Flow Diagrams](#data-flow-diagrams)
8. [Class Relationships](#class-relationships)

---

## 📖 Project Overview

The **Firewall Security Application** is a JavaFX desktop application that:
- Captures network packets from your computer's network interfaces
- Applies firewall rules to filter packets (Block/Allow)
- Stores captured packets in a MySQL database using Hibernate/JPA
- Provides a modern UI for monitoring and managing network traffic

### Technology Stack
| Component | Technology |
|-----------|------------|
| UI Framework | JavaFX + FXML |
| Packet Capture | Pcap4J (requires Npcap) |
| Database | MySQL (via Docker) |
| ORM | Hibernate/JPA |
| Build Tool | Maven |

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        FIREWALL SECURITY APPLICATION                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                         UI LAYER (JavaFX)                         │   │
│  │  ┌─────────────────┐    ┌─────────────────────────────────────┐  │   │
│  │  │   MainView.fxml │    │         CaptureView.fxml            │  │   │
│  │  │  (Interface     │───▶│  (Capture + History + Rule Config)  │  │   │
│  │  │   Selection)    │    │                                     │  │   │
│  │  └────────┬────────┘    └────────────────┬────────────────────┘  │   │
│  │           │                              │                        │   │
│  │  ┌────────▼────────┐    ┌────────────────▼────────────────────┐  │   │
│  │  │Interface        │    │         CaptureController           │  │   │
│  │  │Controller       │───▶│   - Rule management                 │  │   │
│  │  │                 │    │   - Capture control                 │  │   │
│  │  └─────────────────┘    │   - History display                 │  │   │
│  │                         └────────────────┬────────────────────┘  │   │
│  └──────────────────────────────────────────┼───────────────────────┘   │
│                                             │                            │
│  ┌──────────────────────────────────────────┼───────────────────────┐   │
│  │                      LOGIC LAYER                                  │   │
│  │         ┌────────────────┴────────────────┐                      │   │
│  │         ▼                                 ▼                      │   │
│  │  ┌─────────────────┐              ┌─────────────────┐            │   │
│  │  │ PacketCapture   │              │ FirewallEngine  │            │   │
│  │  │ (Pcap4J wrapper)│              │ (Rule evaluator)│            │   │
│  │  └────────┬────────┘              └───────┬─────────┘            │   │
│  │           │                               │                      │   │
│  │           │                    ┌──────────┴──────────┐           │   │
│  │           │                    ▼                     ▼           │   │
│  │           │          ┌─────────────────┐  ┌──────────────────┐   │   │
│  │           │          │ IPBlockRule     │  │ PortBlockRule    │   │   │
│  │           │          │ IPRangeBlockRule│  │ ProtocolBlockRule│   │   │
│  │           │          │ PayloadSizeRule │  │                  │   │   │
│  │           │          └─────────────────┘  └──────────────────┘   │   │
│  └───────────┼──────────────────────────────────────────────────────┘   │
│              │                                                           │
│  ┌───────────┼──────────────────────────────────────────────────────┐   │
│  │           │           DOMAIN LAYER                                │   │
│  │           ▼                                                       │   │
│  │  ┌─────────────────┐              ┌─────────────────┐            │   │
│  │  │     Packet      │              │  ProtocolType   │            │   │
│  │  │ (DTO for live   │              │  (TCP, UDP,     │            │   │
│  │  │  packet data)   │              │   ICMP, UNKNOWN)│            │   │
│  │  └────────┬────────┘              └─────────────────┘            │   │
│  └───────────┼──────────────────────────────────────────────────────┘   │
│              │                                                           │
│  ┌───────────┼──────────────────────────────────────────────────────┐   │
│  │           │         PERSISTENCE LAYER (Hibernate/JPA)            │   │
│  │           ▼                                                       │   │
│  │  ┌─────────────────┐              ┌─────────────────┐            │   │
│  │  │PacketRepository │              │ CapturedPacket  │            │   │
│  │  │ (Database ops)  │─────────────▶│ (JPA Entity)    │            │   │
│  │  └────────┬────────┘              └─────────────────┘            │   │
│  └───────────┼──────────────────────────────────────────────────────┘   │
│              │                                                           │
│              ▼                                                           │
│     ┌─────────────────┐                                                  │
│     │   MySQL (Docker)│                                                  │
│     │  captured_packets│                                                 │
│     └─────────────────┘                                                  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Application Startup Flow

### Step 1: Main Entry Point - `MainApp.java`

When you run the application, execution starts here:

```java
// MainApp.java - The entry point
public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);  // ① JavaFX starts here
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ② Load the first page (MainView.fxml)
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));

        // ③ Create the window with dimensions
        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setTitle("Firewall Security");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(550);
        primaryStage.show();  // ④ Display the window
    }

    @Override
    public void stop() {
        PacketRepository.shutdown();  // ⑤ Cleanup database when closing
    }
}
```

### Startup Sequence Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                    APPLICATION STARTUP SEQUENCE                       │
└──────────────────────────────────────────────────────────────────────┘

    User                  MainApp              FXMLLoader          InterfaceController
      │                      │                     │                       │
      │  Run Application     │                     │                       │
      │─────────────────────▶│                     │                       │
      │                      │                     │                       │
      │                      │ main() called       │                       │
      │                      │────────────────┐    │                       │
      │                      │                │    │                       │
      │                      │◀───────────────┘    │                       │
      │                      │                     │                       │
      │                      │ launch(args)        │                       │
      │                      │────────────────┐    │                       │
      │                      │                │    │                       │
      │                      │◀───────────────┘    │                       │
      │                      │                     │                       │
      │                      │ start(Stage stage)  │                       │
      │                      │────────────────┐    │                       │
      │                      │                │    │                       │
      │                      │◀───────────────┘    │                       │
      │                      │                     │                       │
      │                      │ Load MainView.fxml  │                       │
      │                      │────────────────────▶│                       │
      │                      │                     │                       │
      │                      │                     │ Create Controller      │
      │                      │                     │──────────────────────▶│
      │                      │                     │                       │
      │                      │                     │                       │ initialize()
      │                      │                     │                       │─────────┐
      │                      │                     │                       │         │
      │                      │                     │                       │◀────────┘
      │                      │                     │                       │
      │                      │                     │ Return Parent (UI)    │
      │                      │◀────────────────────│                       │
      │                      │                     │                       │
      │                      │ Show Window         │                       │
      │◀─────────────────────│                     │                       │
      │                      │                     │                       │
      │  [Window Displayed]  │                     │                       │
      │                      │                     │                       │
```

---

## 🔀 Page Navigation Flow

The application has **2 main pages**:

### Page 1: Interface Selection (MainView.fxml)
**Controller:** `InterfaceController.java`

### Page 2: Packet Capture & History (CaptureView.fxml)
**Controller:** `CaptureController.java` (with 2 tabs: Capture & History)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        PAGE NAVIGATION FLOW                              │
└─────────────────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────────────────────────┐
    │                                                                 │
    │                   ┌─────────────────────┐                       │
    │                   │    MainView.fxml    │                       │
    │                   │  (InterfaceController)                      │
    │                   │                     │                       │
    │                   │  • Scan Interfaces  │                       │
    │                   │  • Select Interface │                       │
    │                   │  • DB Status Check  │                       │
    │                   └──────────┬──────────┘                       │
    │                              │                                  │
    │                              │ onSelectInterface()              │
    │                              │ (User clicks "Select Interface") │
    │                              ▼                                  │
    │                   ┌─────────────────────┐                       │
    │                   │   CaptureView.fxml  │                       │
    │                   │ (CaptureController) │                       │
    │                   │                     │                       │
    │                   │  ┌───────┬───────┐  │                       │
    │                   │  │Capture│History│  │◀────── Tab Navigation │
    │                   │  │ Tab   │ Tab   │  │                       │
    │                   │  └───┬───┴───┬───┘  │                       │
    │                   │      │       │      │                       │
    │                   │      ▼       ▼      │                       │
    │                   │  • Rules   • View   │                       │
    │                   │  • Start   • Refresh│                       │
    │                   │  • Stop    • Delete │                       │
    │                   │  • Export           │                       │
    │                   └──────────┬──────────┘                       │
    │                              │                                  │
    │                              │ onBack()                         │
    │                              │ (User clicks "Network Interfaces")│
    │                              ▼                                  │
    │                   ┌─────────────────────┐                       │
    │                   │    MainView.fxml    │                       │
    │                   │  (Back to start)    │                       │
    │                   └─────────────────────┘                       │
    │                                                                 │
    └─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Detailed Component Breakdown

### 1️⃣ InterfaceController - First Page

**File:** `src/main/java/com/firewall/ui/controller/InterfaceController.java`

**Purpose:** Scan and select network interfaces

```java
public class InterfaceController {
    
    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        interfaceListView.setItems(interfaces);  // ① Setup list view
        
        // ② Custom cell styling for better appearance
        interfaceListView.setCellFactory(lv -> new ListCell<>() { ... });
        
        // ③ Enable select button when interface is chosen
        interfaceListView.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, newVal) -> {
                selectButton.setDisable(newVal == null);
            });
        
        // ④ Check database connection in background
        checkDatabaseConnection();
    }
    
    // ===== SCAN INTERFACES =====
    @FXML
    public void onScanInterfaces() {
        // Runs in background thread to avoid UI freeze
        new Thread(() -> {
            try {
                // ① Get all network interfaces using Pcap4J
                networkInterfaces = PacketCapture.getNetworkInterfaces();
                
                // ② Update UI on JavaFX thread
                Platform.runLater(() -> {
                    for (PcapNetworkInterface nif : networkInterfaces) {
                        interfaces.add(nif.getDescription());
                    }
                    statusLabel.setText("Scan complete");
                });
            } catch (Exception e) {
                // Handle Npcap not installed error
            }
        }).start();
    }
    
    // ===== SELECT INTERFACE & NAVIGATE =====
    @FXML
    public void onSelectInterface() {
        int selectedIndex = interfaceListView.getSelectionModel().getSelectedIndex();
        PcapNetworkInterface selectedInterface = networkInterfaces.get(selectedIndex);
        
        // ① Load the capture page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CaptureView.fxml"));
        Parent root = loader.load();
        
        // ② Pass the selected interface to the new controller
        CaptureController controller = loader.getController();
        controller.setNetworkInterface(selectedInterface, selectedIndex);
        
        // ③ Switch to new scene
        Stage stage = (Stage) selectButton.getScene().getWindow();
        stage.setScene(new Scene(root, 1000, 650));
    }
}
```

### 2️⃣ CaptureController - Second Page

**File:** `src/main/java/com/firewall/ui/controller/CaptureController.java`

**Purpose:** Manage rules, capture packets, view history

#### Core Components:

```java
public class CaptureController {
    
    // ===== KEY OBJECTS =====
    private final FirewallEngine engine = new FirewallEngine();  // Rule evaluator
    private final PacketCapture capture = new PacketCapture();   // Packet sniffer
    private final PacketRepository repository = new PacketRepository(); // Database
    
    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        rulesListView.setItems(rulesList);
        packetsTable.setItems(packetRows);
        historyTable.setItems(historyRows);
        
        protocolCombo.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP"));
        
        setupCaptureTable();  // Configure table columns
        setupHistoryTable();
        
        sessionId = UUID.randomUUID().toString().substring(0, 8); // Generate session ID
        checkDatabase();
    }
    
    // ===== RULE MANAGEMENT =====
    @FXML
    public void onAddIpRule() {
        String ip = ipBlockField.getText().trim();
        engine.addRule(new IPBlockRule("Block " + ip, 100, ip));  // Add to engine
        rulesList.add("🚫 IP: " + ip);  // Show in UI
    }
    
    @FXML
    public void onAddPortRule() {
        int port = Integer.parseInt(portBlockField.getText().trim());
        engine.addRule(new PortBlockRule("Block Port " + port, 90, port));
        rulesList.add("🚫 Port: " + port);
    }
    
    // Similar for: Protocol, Port Range, IP Range, Payload Size rules
    
    // ===== PACKET CAPTURE =====
    @FXML
    public void onStartCapture() {
        isCapturing = true;
        
        new Thread(() -> {
            capture.startCapture(interfaceIndex, packet -> {
                // ① Check if packet should be blocked
                boolean blocked = engine.shouldBlock(packet);
                FilterRule matchedRule = blocked ? engine.getMatchingRule(packet) : null;
                
                // ② Create row for UI display
                PacketRow row = new PacketRow(
                    TIME_FORMAT.format(packet.getTimestamp()),
                    packet.getSourceIP(),
                    packet.getSourcePort().toString(),
                    packet.getDestIP(),
                    packet.getDestPort().toString(),
                    packet.getProtocol().name(),
                    blocked ? "BLOCKED" : "ALLOWED"
                );
                
                // ③ Create entity for database
                CapturedPacket entity = CapturedPacket.builder()
                    .sourceIp(packet.getSourceIP())
                    .destIp(packet.getDestIP())
                    // ... other fields
                    .matchedRule(matchedRule != null ? matchedRule.getDescription() : null)
                    .sessionId(sessionId)
                    .build();
                
                capturedPackets.add(entity);
                
                // ④ Update UI
                Platform.runLater(() -> packetRows.add(0, row));
                
            }, maxPackets);
        }).start();
    }
    
    // When capture stops, save to database
    private void stopCaptureInternal() {
        isCapturing = false;
        capture.stopCapture();
        
        if (dbConnected && !capturedPackets.isEmpty()) {
            repository.saveAll(new ArrayList<>(capturedPackets));  // Batch save
        }
    }
}
```

### 3️⃣ PacketCapture - Network Sniffing

**File:** `src/main/java/com/firewall/capture/PacketCapture.java`

**Purpose:** Low-level packet capture using Pcap4J

```java
public class PacketCapture {
    
    private static final int SNAPSHOT_LENGTH = 65536;
    private static final int READ_TIMEOUT = 50; // ms
    
    private PcapHandle handle;
    private volatile boolean capturing = false;
    
    // ===== GET AVAILABLE INTERFACES =====
    public static List<PcapNetworkInterface> getNetworkInterfaces() throws PcapNativeException {
        return Pcaps.findAllDevs();  // Uses Npcap to find all adapters
    }
    
    // ===== START CAPTURING =====
    public void startCapture(int interfaceIndex, Consumer<Packet> packetHandler, int maxPackets) {
        try {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            PcapNetworkInterface nif = allDevs.get(interfaceIndex);
            
            // ① Open the interface in promiscuous mode
            handle = nif.openLive(SNAPSHOT_LENGTH, 
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 
                READ_TIMEOUT);
            
            capturing = true;
            int count = 0;
            
            // ② Loop to capture packets
            while (capturing && (maxPackets == 0 || count < maxPackets)) {
                org.pcap4j.packet.Packet rawPacket = handle.getNextPacket();
                if (rawPacket != null) {
                    // ③ Convert raw packet to our domain object
                    Packet packet = convertPacket(rawPacket);
                    if (packet != null) {
                        packetHandler.accept(packet);  // ④ Pass to callback
                        count++;
                    }
                }
            }
        } finally {
            stopCapture();
        }
    }
    
    // ===== CONVERT RAW PACKET =====
    private Packet convertPacket(org.pcap4j.packet.Packet rawPacket) {
        // Extract IPv4 packet
        IpV4Packet ipPacket = rawPacket.get(IpV4Packet.class);
        if (ipPacket == null) return null; // Skip non-IPv4
        
        Packet.PacketBuilder builder = Packet.builder()
            .sourceIP(ipPacket.getHeader().getSrcAddr().getHostAddress())
            .destIP(ipPacket.getHeader().getDstAddr().getHostAddress())
            .timestamp(Instant.now())
            .payloadSize(rawPacket.length());
        
        // Check for TCP
        TcpPacket tcpPacket = rawPacket.get(TcpPacket.class);
        if (tcpPacket != null) {
            builder.protocol(ProtocolType.TCP)
                .sourcePort(tcpPacket.getHeader().getSrcPort().valueAsInt())
                .destPort(tcpPacket.getHeader().getDstPort().valueAsInt());
            return builder.build();
        }
        
        // Check for UDP
        UdpPacket udpPacket = rawPacket.get(UdpPacket.class);
        if (udpPacket != null) {
            builder.protocol(ProtocolType.UDP)
                .sourcePort(udpPacket.getHeader().getSrcPort().valueAsInt())
                .destPort(udpPacket.getHeader().getDstPort().valueAsInt());
            return builder.build();
        }
        
        // Other protocols (ICMP, etc.)
        builder.protocol(ProtocolType.UNKNOWN);
        return builder.build();
    }
}
```

### 4️⃣ FirewallEngine - Rule Evaluation

**File:** `src/main/java/com/firewall/logic/FirewallEngine.java`

**Purpose:** Manage and evaluate firewall rules

```java
public class FirewallEngine {
    
    private final List<FilterRule> rules = new ArrayList<>();
    
    // ===== ADD RULE (sorted by priority) =====
    public void addRule(FilterRule rule) {
        rules.add(rule);
        sortRulesByPriority();  // Higher priority = checked first
    }
    
    // ===== CHECK IF PACKET SHOULD BE BLOCKED =====
    public boolean shouldBlock(Packet packet) {
        for (FilterRule rule : rules) {
            if (rule.matches(packet)) {
                return true;  // First matching rule blocks
            }
        }
        return false;  // No rules matched = ALLOW
    }
    
    // ===== GET MATCHING RULE =====
    public FilterRule getMatchingRule(Packet packet) {
        for (FilterRule rule : rules) {
            if (rule.matches(packet)) {
                return rule;
            }
        }
        return null;
    }
    
    // ===== SORT BY PRIORITY (higher first) =====
    private void sortRulesByPriority() {
        rules.sort((r1, r2) -> {
            if (r1 instanceof AbstractRule ar1 && r2 instanceof AbstractRule ar2) {
                return Integer.compare(ar2.getPriority(), ar1.getPriority());
            }
            return 0;
        });
    }
}
```

### 5️⃣ Rule Types (Filter Rules)

**Directory:** `src/main/java/com/firewall/logic/rules/`

| Rule Class | Priority | What it Blocks |
|------------|----------|----------------|
| `IPBlockRule` | 100 | Specific IP address (source/dest/both) |
| `IPRangeBlockRule` | 95 | CIDR range (e.g., 192.168.1.0/24) |
| `PortBlockRule` | 90 | Single port or port range |
| `ProtocolBlockRule` | 80 | All traffic of a protocol (TCP/UDP/ICMP) |
| `PayloadSizeRule` | 75 | Packets larger than specified bytes |

**Example - PortBlockRule:**

```java
public class PortBlockRule extends AbstractRule {
    
    private final int portStart;
    private final int portEnd;
    private final PortMatchMode matchMode;
    
    public enum PortMatchMode {
        SOURCE,      // Block if source port matches
        DESTINATION, // Block if destination port matches
        BOTH         // Block if either port matches
    }
    
    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) return false;
        
        return switch (matchMode) {
            case SOURCE -> isInRange(packet.getSourcePort());
            case DESTINATION -> isInRange(packet.getDestPort());
            case BOTH -> isInRange(packet.getSourcePort()) || 
                         isInRange(packet.getDestPort());
        };
    }
    
    private boolean isInRange(Integer port) {
        if (port == null) return false;
        return port >= portStart && port <= portEnd;
    }
}
```

### 6️⃣ Persistence Layer (Database)

**Files:**
- `PacketRepository.java` - Database operations
- `CapturedPacket.java` - JPA Entity

```java
// ===== JPA ENTITY =====
@Entity
@Table(name = "captured_packets")
public class CapturedPacket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 45)
    private String sourceIp;
    
    @Column(nullable = false, length = 45)
    private String destIp;
    
    private Integer sourcePort;
    private Integer destPort;
    
    @Enumerated(EnumType.STRING)
    private ProtocolType protocol;
    
    private int payloadSize;
    private Instant capturedAt;
    private String matchedRule;
    private String sessionId;
    private String interfaceName;
}

// ===== REPOSITORY =====
public class PacketRepository {
    
    private static EntityManagerFactory emf;
    
    public static void initialize() {
        emf = Persistence.createEntityManagerFactory("firewall-pu");
    }
    
    public void saveAll(List<CapturedPacket> packets) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (int i = 0; i < packets.size(); i++) {
                em.persist(packets.get(i));
                if (i % 50 == 0) {  // Flush every 50 for performance
                    em.flush();
                    em.clear();
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    public List<CapturedPacket> findRecent(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM CapturedPacket p ORDER BY p.capturedAt DESC",
                CapturedPacket.class)
                .setMaxResults(limit)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
```

---

## 🎬 Complete Workflow Scenario

### Scenario: User captures 50 packets and blocks port 80

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE USER WORKFLOW SCENARIO                        │
└──────────────────────────────────────────────────────────────────────────┘

    User                    Application                         Database
      │                          │                                  │
      │ 1. Run application       │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │ [MainView displayed]     │                                  │
      │◀─────────────────────────│                                  │
      │                          │                                  │
      │ 2. Click "Scan"          │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │                          │ PacketCapture.getNetworkInterfaces()
      │                          │────────────────┐                 │
      │                          │                │ (Pcap4J)        │
      │                          │◀───────────────┘                 │
      │                          │                                  │
      │ [List of interfaces]     │                                  │
      │◀─────────────────────────│                                  │
      │                          │                                  │
      │ 3. Select "Wi-Fi"        │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │ 4. Click "Select"        │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │                          │ Load CaptureView.fxml            │
      │                          │ Pass interface to controller     │
      │                          │────────────────┐                 │
      │                          │                │                 │
      │                          │◀───────────────┘                 │
      │                          │                                  │
      │ [CaptureView displayed]  │  Check database connection       │
      │◀─────────────────────────│─────────────────────────────────▶│
      │                          │                                  │
      │                          │  Connected ✓                     │
      │                          │◀─────────────────────────────────│
      │                          │                                  │
      │ 5. Enter "80" in port    │                                  │
      │    Click "Add"           │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │                          │ engine.addRule(new PortBlockRule(80))
      │                          │ rulesList.add("🚫 Port: 80")     │
      │                          │────────────────┐                 │
      │                          │                │                 │
      │                          │◀───────────────┘                 │
      │                          │                                  │
      │ [Rule appears in list]   │                                  │
      │◀─────────────────────────│                                  │
      │                          │                                  │
      │ 6. Set max packets: 50   │                                  │
      │    Click "Start"         │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │                          │ Start background thread          │
      │                          │ capture.startCapture(...)       │
      │                          │────────────────┐                 │
      │                          │                │ (Loop)          │
      │                          │                │                 │
      │                          │   For each packet:               │
      │                          │   ├─ blocked = engine.shouldBlock(packet)
      │                          │   ├─ Create PacketRow for UI     │
      │                          │   ├─ Create CapturedPacket entity│
      │                          │   └─ Platform.runLater() update  │
      │                          │                │                 │
      │ [Real-time table updates]│                │                 │
      │◀─────────────────────────│                │                 │
      │                          │                │                 │
      │                          │   ... 50 packets captured ...   │
      │                          │                │                 │
      │                          │◀───────────────┘                 │
      │                          │                                  │
      │                          │ stopCaptureInternal()            │
      │                          │────────────────┐                 │
      │                          │                │                 │
      │                          │ repository.saveAll(capturedPackets)
      │                          │─────────────────────────────────▶│
      │                          │                                  │
      │                          │                      INSERT      │
      │                          │                      50 rows     │
      │                          │◀─────────────────────────────────│
      │                          │                │                 │
      │                          │◀───────────────┘                 │
      │                          │                                  │
      │ [Capture complete]       │                                  │
      │ [Saved 50 packets]       │                                  │
      │◀─────────────────────────│                                  │
      │                          │                                  │
      │ 7. Click "History" tab   │                                  │
      │─────────────────────────▶│                                  │
      │                          │                                  │
      │                          │ onRefreshHistory()               │
      │                          │ repository.findRecent(200)       │
      │                          │─────────────────────────────────▶│
      │                          │                                  │
      │                          │  Return list of CapturedPacket   │
      │                          │◀─────────────────────────────────│
      │                          │                                  │
      │                          │ Populate historyTable            │
      │                          │────────────────┐                 │
      │                          │                │                 │
      │                          │◀───────────────┘                 │
      │                          │                                  │
      │ [History table shown]    │                                  │
      │◀─────────────────────────│                                  │
      │                          │                                  │
```

---

## 📊 Data Flow Diagrams

### Packet Processing Flow

```
                                  ┌──────────────────┐
                                  │  Network Traffic │
                                  │   (Internet)     │
                                  └────────┬─────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │ Network Adapter  │
                                  │ (Wi-Fi/Ethernet) │
                                  └────────┬─────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │    Npcap/Pcap    │
                                  │  (Low-level API) │
                                  └────────┬─────────┘
                                           │
                                           ▼
                   ┌───────────────────────────────────────────┐
                   │              PacketCapture.java           │
                   │  ┌────────────────────────────────────┐   │
                   │  │ startCapture() loop:               │   │
                   │  │   raw = handle.getNextPacket()    │   │
                   │  │   packet = convertPacket(raw)     │   │
                   │  │   callback(packet)                 │   │
                   │  └────────────────────────────────────┘   │
                   └───────────────────────┬───────────────────┘
                                           │
                                           ▼
                   ┌───────────────────────────────────────────┐
                   │               Packet DTO                  │
                   │  ┌────────────────────────────────────┐   │
                   │  │ sourceIP: "192.168.1.100"         │   │
                   │  │ destIP: "142.250.185.78"          │   │
                   │  │ sourcePort: 54321                 │   │
                   │  │ destPort: 80                      │   │
                   │  │ protocol: TCP                     │   │
                   │  │ timestamp: 2024-01-15T10:30:00Z   │   │
                   │  │ payloadSize: 1500                 │   │
                   │  └────────────────────────────────────┘   │
                   └───────────────────────┬───────────────────┘
                                           │
                                           ▼
                   ┌───────────────────────────────────────────┐
                   │            FirewallEngine.java            │
                   │                                           │
                   │  shouldBlock(packet):                     │
                   │    for each rule:                         │
                   │      if rule.matches(packet):             │
                   │        return true (BLOCK)                │
                   │    return false (ALLOW)                   │
                   │                                           │
                   └───────────────────────┬───────────────────┘
                                           │
                          ┌────────────────┴────────────────┐
                          │                                 │
                          ▼                                 ▼
                  ┌───────────────┐                ┌───────────────┐
                  │   BLOCKED     │                │   ALLOWED     │
                  │               │                │               │
                  │ • Red in UI   │                │ • Green in UI │
                  │ • Rule logged │                │ • No rule     │
                  └───────┬───────┘                └───────┬───────┘
                          │                                 │
                          └────────────────┬────────────────┘
                                           │
                                           ▼
                   ┌───────────────────────────────────────────┐
                   │           CaptureController.java          │
                   │                                           │
                   │  • Create PacketRow for TableView         │
                   │  • Create CapturedPacket for database     │
                   │  • Update UI via Platform.runLater()      │
                   │                                           │
                   └───────────────────────┬───────────────────┘
                                           │
                          ┌────────────────┴────────────────┐
                          │                                 │
                          ▼                                 ▼
                  ┌───────────────┐                ┌───────────────┐
                  │  UI Display   │                │   Database    │
                  │  (TableView)  │                │   (MySQL)     │
                  │               │                │               │
                  │ Real-time     │                │ saveAll()     │
                  │ updates       │                │ when stopped  │
                  └───────────────┘                └───────────────┘
```

---

## 🔗 Class Relationships

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CLASS RELATIONSHIP DIAGRAM                       │
└─────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────┐
                    │      MainApp        │
                    │    (Entry Point)    │
                    └──────────┬──────────┘
                               │ starts
                               ▼
                    ┌─────────────────────┐
                    │ InterfaceController │
                    │                     │
                    │ + onScanInterfaces()│
                    │ + onSelectInterface()│
                    └──────────┬──────────┘
                               │ uses              navigates to
                               ▼                        │
                    ┌─────────────────────┐             │
                    │    PacketCapture    │             │
                    │                     │             │
                    │ + getNetworkInterfaces()          │
                    │ + startCapture()    │             │
                    │ + stopCapture()     │             │
                    │ + convertPacket()   │             │
                    └──────────┬──────────┘             │
                               │ produces               │
                               ▼                        │
                    ┌─────────────────────┐             ▼
                    │       Packet        │  ┌─────────────────────┐
                    │       (DTO)         │  │  CaptureController  │
                    │                     │  │                     │
                    │ - sourceIP          │◀─│ + initialize()      │
                    │ - destIP            │  │ + onAddIpRule()     │
                    │ - sourcePort        │  │ + onStartCapture()  │
                    │ - destPort          │  │ + onStopCapture()   │
                    │ - protocol          │  │ + showHistoryTab()  │
                    │ - timestamp         │  └──────────┬──────────┘
                    │ - payloadSize       │             │
                    └─────────────────────┘             │ uses
                               │                        │
                               │                        ▼
                               │         ┌─────────────────────┐
                               │         │   FirewallEngine    │
                               │         │                     │
                               └────────▶│ + addRule()         │
                                         │ + shouldBlock()     │
                                         │ + getMatchingRule() │
                                         │ + clearRules()      │
                                         └──────────┬──────────┘
                                                    │ contains
                                                    ▼
                    ┌───────────────────────────────────────────────────┐
                    │                  RULE HIERARCHY                   │
                    │                                                   │
                    │         ┌─────────────────────┐                   │
                    │         │   <<interface>>     │                   │
                    │         │    FilterRule       │                   │
                    │         │                     │                   │
                    │         │ + matches(Packet)   │                   │
                    │         │ + getDescription()  │                   │
                    │         └──────────┬──────────┘                   │
                    │                    │ implements                   │
                    │                    ▼                              │
                    │         ┌─────────────────────┐                   │
                    │         │   <<abstract>>      │                   │
                    │         │   AbstractRule      │                   │
                    │         │                     │                   │
                    │         │ - name: String      │                   │
                    │         │ - priority: int     │                   │
                    │         │ - enabled: boolean  │                   │
                    │         └──────────┬──────────┘                   │
                    │                    │ extends                      │
                    │    ┌───────────────┼───────────────┐              │
                    │    ▼               ▼               ▼              │
                    │ ┌──────────┐ ┌──────────┐ ┌────────────┐          │
                    │ │IPBlockRule│ │PortBlock │ │ProtocolBlock│        │
                    │ │          │ │  Rule    │ │   Rule     │          │
                    │ │-blockedIP│ │-portStart│ │-blockedProt│          │
                    │ │-matchMode│ │-portEnd  │ │            │          │
                    │ └──────────┘ │-matchMode│ └────────────┘          │
                    │              └──────────┘                         │
                    │    ┌───────────────┬───────────────┐              │
                    │    ▼               ▼                              │
                    │ ┌──────────────┐ ┌────────────────┐               │
                    │ │IPRangeBlock │ │PayloadSizeRule │               │
                    │ │    Rule     │ │                │               │
                    │ │             │ │ -maxSize       │               │
                    │ │ -cidrBlock  │ │ -mode          │               │
                    │ └──────────────┘ └────────────────┘               │
                    └───────────────────────────────────────────────────┘

                    ┌───────────────────────────────────────────────────┐
                    │               PERSISTENCE LAYER                   │
                    │                                                   │
                    │  ┌─────────────────────┐  ┌─────────────────────┐ │
                    │  │  PacketRepository   │  │  CapturedPacket     │ │
                    │  │                     │  │  (JPA Entity)       │ │
                    │  │ + initialize()      │  │                     │ │
                    │  │ + shutdown()        │─▶│ @Entity             │ │
                    │  │ + save()            │  │ @Table("captured_   │ │
                    │  │ + saveAll()         │  │        packets")    │ │
                    │  │ + findRecent()      │  │                     │ │
                    │  │ + findBySession()   │  │ - id: Long          │ │
                    │  └─────────────────────┘  │ - sourceIp: String  │ │
                    │                           │ - destIp: String    │ │
                    │                           │ - sourcePort: Int   │ │
                    │                           │ - destPort: Int     │ │
                    │                           │ - protocol: Enum    │ │
                    │                           │ - capturedAt: Inst  │ │
                    │                           │ - matchedRule: Str  │ │
                    │                           │ - sessionId: Str    │ │
                    │                           └─────────────────────┘ │
                    └───────────────────────────────────────────────────┘
```

---

## 📝 Summary: Function Call Order

### When Application Starts:
1. `MainApp.main()` → `launch(args)`
2. `MainApp.start(Stage)` → Load `MainView.fxml`
3. `InterfaceController.initialize()` → Setup UI + Check database

### When User Clicks "Scan":
1. `InterfaceController.onScanInterfaces()`
2. `PacketCapture.getNetworkInterfaces()` → Return list of adapters

### When User Selects Interface:
1. `InterfaceController.onSelectInterface()`
2. Load `CaptureView.fxml`
3. `CaptureController.setNetworkInterface()` → Store selected interface
4. `CaptureController.initialize()` → Setup tables + Generate session ID

### When User Adds a Rule:
1. `CaptureController.onAddIpRule()` (or port, protocol, etc.)
2. `FirewallEngine.addRule(new XxxRule(...))` → Add and sort by priority

### When User Starts Capture:
1. `CaptureController.onStartCapture()`
2. Background thread: `PacketCapture.startCapture()`
3. For each packet: `PacketCapture.convertPacket()` → `Packet DTO`
4. `FirewallEngine.shouldBlock(Packet)` → Check rules
5. Create `PacketRow` + `CapturedPacket` → Update UI

### When Capture Stops:
1. `CaptureController.stopCaptureInternal()`
2. `PacketCapture.stopCapture()`
3. `PacketRepository.saveAll()` → Batch insert to database

### When User Views History:
1. `CaptureController.showHistoryTab()`
2. `CaptureController.onRefreshHistory()`
3. `PacketRepository.findRecent(200)` → Query database
4. Populate `historyTable` with results

---

## ✅ Prerequisites to Run

1. **Install Npcap** - Required for packet capture on Windows
2. **Start MySQL via Docker**: `docker-compose up -d`
3. **Run as Administrator** - Required for promiscuous mode
4. **Java 17+** with JavaFX

---

*Document generated for educational purposes - Firewall Security Project*
