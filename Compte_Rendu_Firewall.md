# Compte Rendu Détaillé : Application Firewall Security - Capture et Filtrage de Paquets Réseau

## 1. Objectifs et Structure du Projet

### Objectif Global
L'objectif de ce projet est de développer une application de sécurité réseau permettant la **capture en temps réel de paquets réseau**, leur **filtrage selon des règles configurables** et leur **persistance dans une base de données MySQL** via Docker. L'application dispose d'une interface graphique professionnelle en **JavaFX** suivant les principes de design Windows 11 Fluent.

### Fonctionnalités Principales
- ✅ Capture de paquets réseau en temps réel (TCP, UDP, ICMP)
- ✅ Filtrage multi-critères (IP, Port, Protocole, Taille)
- ✅ Persistance des données via Hibernate/JPA et MySQL
- ✅ Interface graphique professionnelle (JavaFX)
- ✅ Limitation du débit de capture (100 paquets/minute)
- ✅ Export des données en CSV
- ✅ Historique des captures stocké en base de données

### Structure du Projet (Arborescence Maven)
Le projet suit la structure standard Maven avec une architecture en couches bien définie :

```
firewall_security/
├── src/main/java/com/firewall/
│   ├── domain/              # Objets métiers (DTO)
│   │   ├── Packet.java
│   │   └── ProtocolType.java
│   ├── logic/               # Logique métier
│   │   ├── FirewallEngine.java
│   │   └── rules/           # Règles de filtrage
│   │       ├── FilterRule.java
│   │       ├── AbstractRule.java
│   │       ├── IPBlockRule.java
│   │       ├── IPRangeBlockRule.java
│   │       ├── PortBlockRule.java
│   │       ├── ProtocolBlockRule.java
│   │       └── PayloadSizeRule.java
│   ├── capture/             # Capture réseau (Pcap4J)
│   │   └── PacketCapture.java
│   ├── persistence/         # Couche de persistance
│   │   ├── entity/
│   │   │   └── CapturedPacket.java
│   │   └── PacketRepository.java
│   └── ui/                  # Interface graphique
│       └── controller/
│           ├── InterfaceController.java
│           └── CaptureController.java
├── src/main/resources/
│   ├── fxml/                # Vues JavaFX
│   │   ├── MainView.fxml
│   │   └── CaptureView.fxml
│   ├── css/
│   │   └── style.css        # Thème Windows Fluent
│   └── META-INF/
│       └── persistence.xml  # Configuration JPA
├── docker-compose.yml       # Base de données MySQL
└── pom.xml                  # Dépendances Maven
```

---

## 2. Couche Domain : Objets Métiers

### Objectif
Définir les objets de transfert de données (DTO) représentant les paquets réseau et les protocoles supportés.

### 2.1 Énumération ProtocolType (`ProtocolType.java`)
```java
package com.firewall.domain;

public enum ProtocolType {
    TCP,
    UDP,
    ICMP,
    UNKNOWN;

    public static ProtocolType fromString(String protocol) {
        if (protocol == null) return UNKNOWN;
        try {
            return valueOf(protocol.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
```

**Analyse :**
- L'énumération garantit un typage fort pour les protocoles supportés
- La méthode `fromString` permet une conversion sécurisée depuis une chaîne de caractères
- Le type `UNKNOWN` gère les protocoles non reconnus sans lever d'exception

### 2.2 Entité Packet (`Packet.java`)
```java
package com.firewall.domain;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class Packet {
    private String sourceIP;
    private String destIP;
    private Integer sourcePort;
    private Integer destPort;
    private ProtocolType protocol;
    private Instant timestamp;
    private int payloadSize;

    public static Packet createTestPacket(String srcIp, String dstIp, 
                                          int srcPort, int dstPort, 
                                          ProtocolType proto) {
        return Packet.builder()
                .sourceIP(srcIp)
                .destIP(dstIp)
                .sourcePort(srcPort)
                .destPort(dstPort)
                .protocol(proto)
                .timestamp(Instant.now())
                .payloadSize(100)
                .build();
    }
}
```

**Concepts clés :**
- **Lombok `@Data`** : Génère automatiquement getters, setters, `equals()`, `hashCode()` et `toString()`
- **Pattern Builder** : Permet une construction fluide et lisible des objets
- **`Instant`** : Type moderne Java 8+ pour les horodatages avec précision nanoseconde

---

## 3. Couche Logic : Règles de Filtrage

### Objectif
Implémenter le pattern **Strategy** pour les règles de filtrage, permettant d'ajouter facilement de nouveaux types de règles.

### 3.1 Interface FilterRule (`FilterRule.java`)
```java
package com.firewall.logic.rules;

import com.firewall.domain.Packet;

public interface FilterRule {
    boolean matches(Packet packet);
    String getDescription();
    int getPriority();
    boolean isEnabled();
}
```

**Pourquoi une interface ?**
- Définit le **contrat** que toutes les règles doivent respecter
- Permet le **polymorphisme** : traiter toutes les règles de manière uniforme
- Facilite les **tests unitaires** via des mocks

### 3.2 Classe Abstraite AbstractRule (`AbstractRule.java`)
```java
package com.firewall.logic.rules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractRule implements FilterRule {
    protected String name;
    protected int priority;
    protected boolean enabled = true;

    protected AbstractRule(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}
```

**Pattern Template Method :**
- Factorise le code commun (nom, priorité, état activé)
- Les classes filles n'implémentent que la logique de matching

### 3.3 Règle de Blocage IP (`IPBlockRule.java`)
```java
package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

@Getter
public class IPBlockRule extends AbstractRule {
    
    public enum MatchMode { SOURCE, DESTINATION, BOTH }
    
    private final String blockedIP;
    private final MatchMode matchMode;

    public IPBlockRule(String name, int priority, String blockedIP) {
        this(name, priority, blockedIP, MatchMode.BOTH);
    }

    public IPBlockRule(String name, int priority, String blockedIP, MatchMode mode) {
        super(name, priority);
        this.blockedIP = blockedIP;
        this.matchMode = mode;
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) return false;
        
        boolean srcMatch = blockedIP.equals(packet.getSourceIP());
        boolean dstMatch = blockedIP.equals(packet.getDestIP());
        
        return switch (matchMode) {
            case SOURCE -> srcMatch;
            case DESTINATION -> dstMatch;
            case BOTH -> srcMatch || dstMatch;
        };
    }

    @Override
    public String getDescription() {
        return String.format("[IPBlockRule] %s - Block: %s (%s)", 
                             name, blockedIP, matchMode);
    }
}
```

**Analyse de la méthode `matches` :**
1. **Vérification préalable** : On vérifie que la règle est active et que le paquet n'est pas null
2. **Comparaison d'IP** : On compare l'IP bloquée avec source et/ou destination selon le mode
3. **Switch Expression (Java 14+)** : Syntaxe moderne et concise pour le switch

### 3.4 Règle de Blocage par Plage IP (`IPRangeBlockRule.java`)
```java
package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

@Getter
public class IPRangeBlockRule extends AbstractRule {
    
    private final String networkAddress;
    private final int prefixLength;
    private final long networkLong;
    private final long maskLong;

    public IPRangeBlockRule(String name, int priority, String cidr) {
        super(name, priority);
        
        String[] parts = cidr.split("/");
        this.networkAddress = parts[0];
        this.prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;
        
        this.networkLong = ipToLong(networkAddress);
        this.maskLong = prefixLength == 0 ? 0 : (0xFFFFFFFFL << (32 - prefixLength));
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) return false;
        
        if (packet.getSourceIP() != null && isInRange(packet.getSourceIP())) {
            return true;
        }
        if (packet.getDestIP() != null && isInRange(packet.getDestIP())) {
            return true;
        }
        return false;
    }

    private boolean isInRange(String ip) {
        try {
            long ipLong = ipToLong(ip);
            return (ipLong & maskLong) == (networkLong & maskLong);
        } catch (Exception e) {
            return false;
        }
    }

    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | (Integer.parseInt(octets[i]) & 0xFF);
        }
        return result;
    }
}
```

**Concepts Réseau :**
- **Notation CIDR** : `192.168.1.0/24` signifie que les 24 premiers bits définissent le réseau
- **Masque binaire** : `0xFFFFFFFFL << (32 - prefixLength)` crée le masque de sous-réseau
- **Test d'appartenance** : `(IP & masque) == (réseau & masque)` vérifie si l'IP est dans la plage

### 3.5 Règle de Blocage par Port (`PortBlockRule.java`)
```java
package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

@Getter
public class PortBlockRule extends AbstractRule {
    
    public enum PortMatchMode { SOURCE, DESTINATION, BOTH }
    
    private final int startPort;
    private final int endPort;
    private final PortMatchMode matchMode;

    public PortBlockRule(String name, int priority, int port) {
        this(name, priority, port, port, PortMatchMode.BOTH);
    }

    public PortBlockRule(String name, int priority, int startPort, 
                         int endPort, PortMatchMode mode) {
        super(name, priority);
        this.startPort = startPort;
        this.endPort = endPort;
        this.matchMode = mode;
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) return false;
        
        boolean srcMatch = isInRange(packet.getSourcePort());
        boolean dstMatch = isInRange(packet.getDestPort());
        
        return switch (matchMode) {
            case SOURCE -> srcMatch;
            case DESTINATION -> dstMatch;
            case BOTH -> srcMatch || dstMatch;
        };
    }

    private boolean isInRange(Integer port) {
        if (port == null) return false;
        return port >= startPort && port <= endPort;
    }
}
```

### 3.6 Règle de Blocage par Protocole (`ProtocolBlockRule.java`)
```java
package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import com.firewall.domain.ProtocolType;
import lombok.Getter;

@Getter
public class ProtocolBlockRule extends AbstractRule {
    
    private final ProtocolType blockedProtocol;

    public ProtocolBlockRule(String name, int priority, ProtocolType protocol) {
        super(name, priority);
        this.blockedProtocol = protocol;
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null || packet.getProtocol() == null) {
            return false;
        }
        return blockedProtocol == packet.getProtocol();
    }
}
```

### 3.7 Règle de Blocage par Taille (`PayloadSizeRule.java`)
```java
package com.firewall.logic.rules;

import com.firewall.domain.Packet;
import lombok.Getter;

@Getter
public class PayloadSizeRule extends AbstractRule {
    
    private final int minSize;
    private final int maxSize;

    public static PayloadSizeRule blockLargerThan(String name, int priority, int size) {
        return new PayloadSizeRule(name, priority, size + 1, Integer.MAX_VALUE);
    }

    @Override
    public boolean matches(Packet packet) {
        if (!enabled || packet == null) return false;
        int size = packet.getPayloadSize();
        return size >= minSize && size <= maxSize;
    }
}
```

**Cas d'utilisation :**
- Bloquer les paquets volumineux qui pourraient indiquer une attaque DDoS
- Filtrer les transferts de fichiers suspects

---

## 4. Moteur de Firewall (`FirewallEngine.java`)

### Objectif
Orchestrer l'évaluation des règles sur les paquets entrants.

```java
package com.firewall.logic;

import com.firewall.domain.Packet;
import com.firewall.logic.rules.FilterRule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FirewallEngine {
    private final List<FilterRule> rules = new ArrayList<>();

    public void addRule(FilterRule rule) {
        rules.add(rule);
        rules.sort(Comparator.comparingInt(FilterRule::getPriority).reversed());
    }

    public boolean shouldBlock(Packet packet) {
        return rules.stream()
                .filter(FilterRule::isEnabled)
                .anyMatch(rule -> rule.matches(packet));
    }

    public FilterRule getMatchingRule(Packet packet) {
        return rules.stream()
                .filter(FilterRule::isEnabled)
                .filter(rule -> rule.matches(packet))
                .findFirst()
                .orElse(null);
    }

    public void clearRules() {
        rules.clear();
    }

    public List<FilterRule> getRules() {
        return new ArrayList<>(rules);
    }
}
```

**Analyse des Streams :**

1. **`shouldBlock()`** :
   ```java
   rules.stream()
        .filter(FilterRule::isEnabled)  // Garde uniquement les règles actives
        .anyMatch(rule -> rule.matches(packet));  // Retourne true dès qu'une règle match
   ```

2. **`getMatchingRule()`** :
   ```java
   rules.stream()
        .filter(FilterRule::isEnabled)
        .filter(rule -> rule.matches(packet))
        .findFirst()  // Retourne Optional<FilterRule>
        .orElse(null);  // Si aucune règle ne match, retourne null
   ```

**Tri par priorité :**
- Les règles de haute priorité (valeur élevée) sont évaluées en premier
- `Comparator.comparingInt(...).reversed()` trie en ordre décroissant

---

## 5. Capture de Paquets avec Pcap4J

### Objectif
Capturer les paquets réseau en temps réel via la bibliothèque **Pcap4J** (wrapper Java de libpcap/Npcap).

### Code Source (`PacketCapture.java`)
```java
package com.firewall.capture;

import com.firewall.domain.Packet;
import com.firewall.domain.ProtocolType;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public class PacketCapture {
    private volatile boolean capturing = false;
    private PcapHandle handle;

    public static List<PcapNetworkInterface> getNetworkInterfaces() 
            throws PcapNativeException {
        return Pcaps.findAllDevs();
    }

    public void startCapture(int interfaceIndex, Consumer<Packet> handler, 
                             int maxPackets) {
        try {
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
            PcapNetworkInterface device = devices.get(interfaceIndex);
            
            handle = device.openLive(65536, 
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 
                    10);
            
            capturing = true;
            int count = 0;
            
            while (capturing && (maxPackets == 0 || count < maxPackets)) {
                try {
                    org.pcap4j.packet.Packet rawPacket = handle.getNextPacket();
                    if (rawPacket != null) {
                        Packet packet = convertPacket(rawPacket);
                        if (packet != null) {
                            handler.accept(packet);
                            count++;
                        }
                    }
                } catch (NotOpenException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeHandle();
        }
    }

    private Packet convertPacket(org.pcap4j.packet.Packet raw) {
        IpV4Packet ipPacket = raw.get(IpV4Packet.class);
        if (ipPacket == null) return null;

        String srcIp = ipPacket.getHeader().getSrcAddr().getHostAddress();
        String dstIp = ipPacket.getHeader().getDstAddr().getHostAddress();
        
        Integer srcPort = null, dstPort = null;
        ProtocolType protocol = ProtocolType.UNKNOWN;
        int payloadSize = 0;

        TcpPacket tcp = raw.get(TcpPacket.class);
        if (tcp != null) {
            srcPort = tcp.getHeader().getSrcPort().valueAsInt();
            dstPort = tcp.getHeader().getDstPort().valueAsInt();
            protocol = ProtocolType.TCP;
            if (tcp.getPayload() != null) {
                payloadSize = tcp.getPayload().length();
            }
        }

        UdpPacket udp = raw.get(UdpPacket.class);
        if (udp != null) {
            srcPort = udp.getHeader().getSrcPort().valueAsInt();
            dstPort = udp.getHeader().getDstPort().valueAsInt();
            protocol = ProtocolType.UDP;
            if (udp.getPayload() != null) {
                payloadSize = udp.getPayload().length();
            }
        }

        IcmpV4CommonPacket icmp = raw.get(IcmpV4CommonPacket.class);
        if (icmp != null) {
            protocol = ProtocolType.ICMP;
        }

        return Packet.builder()
                .sourceIP(srcIp)
                .destIP(dstIp)
                .sourcePort(srcPort)
                .destPort(dstPort)
                .protocol(protocol)
                .timestamp(Instant.now())
                .payloadSize(payloadSize)
                .build();
    }

    public void stopCapture() {
        capturing = false;
        closeHandle();
    }

    private void closeHandle() {
        if (handle != null && handle.isOpen()) {
            handle.close();
        }
    }
}
```

**Concepts Clés :**

1. **Mode Promiscuous** : Capture tous les paquets sur le réseau, pas seulement ceux destinés à la machine
2. **Volatile** : Le mot-clé `volatile` garantit que `capturing` est visible par tous les threads
3. **Consumer<Packet>** : Interface fonctionnelle permettant de passer un callback pour traiter chaque paquet
4. **Pattern de Parsing** : Extraction hiérarchique des couches réseau (IPv4 → TCP/UDP → Payload)

**Prérequis Windows :**
- Installation de **Npcap** (https://npcap.org)
- Exécution en mode **Administrateur**

---

## 6. Persistance avec Hibernate/JPA

### Objectif
Sauvegarder les paquets capturés dans une base de données MySQL via Docker.

### 6.1 Configuration Docker (`docker-compose.yml`)
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: firewall-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: firewall_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

**Commandes Docker :**
```powershell
# Démarrer la base de données
docker compose up -d

# Vérifier le statut
docker ps

# Arrêter les services
docker compose down
```

### 6.2 Configuration JPA (`persistence.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence" version="3.0">
    <persistence-unit name="firewall-pu" transaction-type="RESOURCE_LOCAL">
        <class>com.firewall.persistence.entity.CapturedPacket</class>
        <properties>
            <property name="jakarta.persistence.jdbc.driver" 
                      value="com.mysql.cj.jdbc.Driver"/>
            <property name="jakarta.persistence.jdbc.url" 
                      value="jdbc:mysql://localhost:3306/firewall_db"/>
            <property name="jakarta.persistence.jdbc.user" value="root"/>
            <property name="jakarta.persistence.jdbc.password" value="root"/>
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            <property name="hibernate.show_sql" value="false"/>
        </properties>
    </persistence-unit>
</persistence>
```

**Propriétés importantes :**
- `hibernate.hbm2ddl.auto=update` : Hibernate met à jour le schéma sans supprimer les données
- `transaction-type="RESOURCE_LOCAL"` : Transactions gérées par l'application (pas de serveur JTA)

### 6.3 Entité JPA (`CapturedPacket.java`)
```java
package com.firewall.persistence.entity;

import com.firewall.domain.ProtocolType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "captured_packets", indexes = {
    @Index(name = "idx_source_ip", columnList = "sourceIp"),
    @Index(name = "idx_dest_ip", columnList = "destIp"),
    @Index(name = "idx_session", columnList = "sessionId"),
    @Index(name = "idx_captured_at", columnList = "capturedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapturedPacket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sourceIp;
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
```

**Annotations JPA :**
- `@Entity` : Marque la classe comme entité persistante
- `@Table` : Spécifie le nom de la table et les index
- `@Id` + `@GeneratedValue` : Clé primaire auto-incrémentée
- `@Enumerated(EnumType.STRING)` : Stocke l'enum comme chaîne (pas comme ordinal)
- `@Index` : Crée des index pour optimiser les requêtes fréquentes

### 6.4 Repository (`PacketRepository.java`)
```java
package com.firewall.persistence;

import com.firewall.persistence.entity.CapturedPacket;
import jakarta.persistence.*;
import java.util.List;

public class PacketRepository {
    private static EntityManagerFactory emf;

    public static void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("firewall-pu");
        }
    }

    public void save(CapturedPacket packet) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(packet);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void saveAll(List<CapturedPacket> packets) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (CapturedPacket p : packets) {
                em.persist(p);
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

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

**Pattern Repository :**
- Abstrait les opérations de base de données
- Gestion propre des transactions avec try/finally
- `EntityManagerFactory` en singleton pour éviter les fuites de connexion

---

## 7. Interface Graphique JavaFX

### Objectif
Fournir une interface utilisateur professionnelle suivant le design **Windows 11 Fluent**.

### 7.1 Architecture MVC
- **Model** : Classes domain (`Packet`, `ProtocolType`)
- **View** : Fichiers FXML (`MainView.fxml`, `CaptureView.fxml`)
- **Controller** : Classes contrôleur (`InterfaceController`, `CaptureController`)

### 7.2 Vue Principale (`MainView.fxml`)
```xml
<BorderPane xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.firewall.ui.controller.InterfaceController"
            style="-fx-background-color: #f5f5f5;">
    
    <!-- Sidebar de navigation -->
    <left>
        <VBox prefWidth="220" style="-fx-background-color: #ffffff;">
            <!-- Logo et navigation -->
            <HBox spacing="12" alignment="CENTER_LEFT">
                <Label text="🛡" style="-fx-font-size: 18px;"/>
                <Label text="Firewall Security" 
                       style="-fx-font-size: 16px; -fx-font-weight: bold;"/>
            </HBox>
            
            <!-- Menu de navigation -->
            <HBox style="-fx-background-color: #e5f3ff;">
                <Label text="📡 Network Interfaces"/>
            </HBox>
        </VBox>
    </left>
    
    <!-- Contenu principal -->
    <center>
        <VBox>
            <!-- En-tête -->
            <Label text="Select Network Interface" 
                   style="-fx-font-size: 20px; -fx-font-weight: bold;"/>
            
            <!-- Bouton de scan -->
            <Button fx:id="scanButton" text="Scan" 
                    onAction="#onScanInterfaces"/>
            
            <!-- Liste des interfaces -->
            <ListView fx:id="interfaceListView"/>
            
            <!-- Bouton de sélection -->
            <Button fx:id="selectButton" text="Select Interface" 
                    onAction="#onSelectInterface"/>
        </VBox>
    </center>
</BorderPane>
```

### 7.3 Contrôleur d'Interface (`InterfaceController.java`)
```java
package com.firewall.ui.controller;

import com.firewall.capture.PacketCapture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.pcap4j.core.PcapNetworkInterface;
import java.util.List;

public class InterfaceController {
    @FXML private ListView<String> interfaceListView;
    @FXML private Button scanButton;
    @FXML private Button selectButton;
    @FXML private ProgressIndicator progressIndicator;

    private List<PcapNetworkInterface> networkInterfaces;

    @FXML
    public void onScanInterfaces() {
        progressIndicator.setVisible(true);
        
        new Thread(() -> {
            try {
                networkInterfaces = PacketCapture.getNetworkInterfaces();
                
                Platform.runLater(() -> {
                    for (PcapNetworkInterface nif : networkInterfaces) {
                        String name = nif.getDescription() != null 
                            ? nif.getDescription() : nif.getName();
                        interfaceListView.getItems().add(name);
                    }
                    progressIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    @FXML
    public void onSelectInterface() {
        int index = interfaceListView.getSelectionModel().getSelectedIndex();
        // Navigation vers la page de capture...
    }
}
```

**Concepts JavaFX :**
- `@FXML` : Injection des composants définis dans le FXML
- `Platform.runLater()` : Exécute du code sur le thread JavaFX depuis un autre thread
- `ObservableList` : Liste qui notifie automatiquement la vue des changements

### 7.4 Contrôleur de Capture (`CaptureController.java` - extrait)
```java
@FXML
public void onStartCapture() {
    isCapturing = true;
    startButton.setDisable(true);
    stopButton.setDisable(false);
    
    new Thread(() -> {
        capture.startCapture(interfaceIndex, packet -> {
            boolean blocked = engine.shouldBlock(packet);
            FilterRule matchedRule = blocked ? engine.getMatchingRule(packet) : null;
            
            // Mise à jour de l'UI sur le thread JavaFX
            Platform.runLater(() -> {
                packetRows.add(0, new PacketRow(...));
                packetCountLabel.setText(String.valueOf(count));
            });
            
            // Sauvegarde en base de données
            CapturedPacket entity = CapturedPacket.builder()
                .sourceIp(packet.getSourceIP())
                // ...
                .build();
            capturedPackets.add(entity);
            
        }, maxPackets);
    }).start();
}
```

---

## 8. Thème CSS Windows Fluent (`style.css`)

```css
/* Couleurs Windows 11 */
.root {
    -fx-background-color: #f5f5f5;
}

/* Bouton primaire - Accent bleu Windows */
.btn-primary {
    -fx-background-color: #0078d4;
    -fx-text-fill: white;
    -fx-font-weight: 600;
    -fx-background-radius: 4;
}

.btn-primary:hover {
    -fx-background-color: #106ebe;
}

/* Bouton succès */
.btn-success {
    -fx-background-color: #107c10;
    -fx-text-fill: white;
}

/* Bouton danger */
.btn-danger {
    -fx-background-color: #d13438;
    -fx-text-fill: white;
}

/* Cartes avec ombre */
.card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
}

/* Indicateurs de statut */
.status-blocked {
    -fx-text-fill: #d13438;
    -fx-font-weight: bold;
}

.status-allowed {
    -fx-text-fill: #107c10;
}
```

---

## 9. Guide d'Exécution

### Prérequis
1. **Java 21** (JDK)
2. **Maven 3.9+**
3. **Docker Desktop**
4. **Npcap** (Windows)

### Étapes de Lancement

1. **Démarrer la base de données MySQL :**
   ```powershell
   cd firewall_security
   docker compose up -d
   ```

2. **Compiler le projet :**
   ```powershell
   mvn clean compile
   ```

3. **Lancer l'application :**
   ```powershell
   mvn javafx:run
   ```

4. **Utilisation :**
   - Cliquer sur **Scan** pour détecter les interfaces réseau
   - Sélectionner une interface (WiFi, Ethernet, etc.)
   - Cliquer sur **Select Interface**
   - Ajouter des règles de filtrage (IP, Port, Protocole...)
   - Cliquer sur **Start Capture** pour capturer les paquets
   - Consulter l'onglet **Packet History** pour voir les données en base

---

## 10. Dépendances Maven (`pom.xml`)

```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>
    
    <!-- Capture réseau -->
    <dependency>
        <groupId>org.pcap4j</groupId>
        <artifactId>pcap4j-core</artifactId>
        <version>1.8.2</version>
    </dependency>
    
    <!-- Persistance -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.3.1.Final</version>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.2.0</version>
    </dependency>
    
    <!-- Utilitaires -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </dependency>
</dependencies>
```

---

## 11. Questions Types pour l'Examen

### Architecture & Patterns
1. **Q : Pourquoi utiliser le pattern Strategy pour les règles de filtrage ?**
   - **R :** Il permet d'ajouter de nouveaux types de règles sans modifier le code existant (Open/Closed Principle).

2. **Q : Quel est l'avantage du pattern Repository ?**
   - **R :** Il abstrait les détails de persistance, permettant de changer de base de données sans impacter la logique métier.

### Streams & Lambdas
3. **Q : Expliquez `rules.stream().filter(...).anyMatch(...)`**
   - **R :** On transforme la liste en flux, on filtre les règles actives, puis on vérifie si au moins une correspond au paquet.

4. **Q : Pourquoi utiliser `Consumer<Packet>` dans la capture ?**
   - **R :** C'est une interface fonctionnelle qui permet de passer un comportement (callback) en paramètre.

### Threads & Concurrence
5. **Q : Pourquoi `Platform.runLater()` est-il nécessaire ?**
   - **R :** JavaFX n'est pas thread-safe. Toute modification de l'UI doit se faire sur le thread JavaFX.

6. **Q : À quoi sert le mot-clé `volatile` ?**
   - **R :** Il garantit la visibilité des modifications d'une variable entre plusieurs threads.

### JPA/Hibernate
7. **Q : Différence entre `persist()` et `merge()` ?**
   - **R :** `persist()` insère une nouvelle entité, `merge()` met à jour une entité existante.

8. **Q : Pourquoi fermer l'EntityManager dans un bloc finally ?**
   - **R :** Pour éviter les fuites de connexion, même en cas d'exception.

---

## 12. Résumé des Concepts Clés

| Concept | Utilisation dans le projet |
|---------|---------------------------|
| **Generics** | `FilterRule`, `List<CapturedPacket>` |
| **Streams** | Filtrage et recherche de règles |
| **Lambdas** | `Consumer<Packet>`, callbacks |
| **Pattern Strategy** | Règles de filtrage interchangeables |
| **Pattern Builder** | Construction des objets `Packet` |
| **Pattern Repository** | Abstraction de la persistance |
| **Pattern Singleton** | `EntityManagerFactory` |
| **MVC** | Architecture JavaFX |
| **JPA/Hibernate** | Mapping objet-relationnel |
| **Docker** | Conteneurisation MySQL |
| **Threads** | Capture asynchrone |

---

**Rapport complet pour l'examen Java Avancé - Projet Firewall Security 2026.**
