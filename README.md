# Firewall Security

A real-time network packet capture and filtering application developed in Java.

## Description

**Firewall Security** is a desktop application for network traffic monitoring and security. It captures network packets in real-time, applies configurable filtering rules, and stores captured data in a MySQL database for later analysis.

### Features

- Real-time packet capture using Pcap4J
- Dynamic filtering rules (IP, Port, Protocol, Packet Size)
- Live traffic visualization with ALLOWED/BLOCKED status
- Persistent storage via Hibernate/JPA and MySQL
- Session-based packet history
- CSV export functionality

## Technologies

| Technology | Purpose |
|------------|---------|
| Java 21 | Core language |
| JavaFX 21 | User interface |
| Pcap4J 1.8.2 | Network packet capture |
| Hibernate 6.x | ORM / Database persistence |
| MySQL 8.0 | Database (Docker) |
| Maven | Build and dependency management |
| Lombok | Boilerplate reduction |

## Project Structure

```
firewall_security/
├── src/main/java/com/firewall/
│   ├── MainApp.java                 # Application entry point
│   ├── ui/controller/               # JavaFX controllers
│   │   ├── InterfaceController.java
│   │   └── CaptureController.java
│   ├── capture/                     # Network capture module
│   │   └── PacketCapture.java
│   ├── domain/                      # Domain objects
│   │   ├── Packet.java
│   │   └── ProtocolType.java
│   ├── logic/                       # Business logic
│   │   ├── FirewallEngine.java
│   │   └── rules/
│   │       ├── FilterRule.java
│   │       ├── AbstractRule.java
│   │       ├── IPBlockRule.java
│   │       ├── PortBlockRule.java
│   │       └── ProtocolBlockRule.java
│   └── persistence/                 # Data persistence
│       ├── PacketRepository.java
│       └── entity/CapturedPacket.java
├── src/main/resources/
│   ├── fxml/                        # UI layouts
│   └── META-INF/persistence.xml     # Hibernate configuration
├── docker-compose.yml               # MySQL container
└── pom.xml                          # Maven configuration
```

## Prerequisites

- Java JDK 21 or higher
- Maven 3.8+
- Docker Desktop
- Npcap (Windows) - Required for packet capture

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/YOUNES0101/Firewall-Security.git
cd Firewall-Security
```

### 2. Install Npcap (Windows)

Download and install from [https://npcap.com](https://npcap.com/#download)

Enable "WinPcap API-compatible Mode" during installation.

### 3. Start the database

```bash
docker-compose up -d
```

### 4. Build the project

```bash
mvn clean install
```

### 5. Run the application

```bash
mvn javafx:run
```

**Note:** Administrator privileges are required for packet capture.

## Usage

1. Click **Scan** to detect available network interfaces
2. Select an interface (Wi-Fi, Ethernet, etc.)
3. Click **Select Interface**
4. Configure filtering rules as needed
5. Click **Start** to begin packet capture
6. View captured packets in the table
7. Access **Packet History** to review stored data

## Database Configuration

The application uses MySQL running in Docker:

| Parameter | Value |
|-----------|-------|
| Host | localhost |
| Port | 3306 |
| Database | firewall_db |
| User | firewall |
| Password | firewall123 |

## Architecture

The application follows a layered architecture:

- **UI Layer**: JavaFX controllers handling user interaction
- **Logic Layer**: FirewallEngine with Strategy pattern for rules
- **Capture Layer**: Pcap4J integration for network access
- **Persistence Layer**: Hibernate/JPA for database operations

### Design Patterns

- **Strategy Pattern**: FilterRule implementations
- **Repository Pattern**: Data access abstraction
- **MVC**: UI architecture
- **Builder Pattern**: Object construction (Lombok)

## Author

**YOUNES CHEMMAOUI**

Email: youneschemmaoui1@gmail.com

## License

This project was developed as part of the Advanced Java course at EMSI.
