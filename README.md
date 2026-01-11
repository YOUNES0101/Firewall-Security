# 🛡️ Firewall Security - Network Packet Analyzer

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)
![Hibernate](https://img.shields.io/badge/Hibernate-6.x-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)

## 📖 Description

**Firewall Security** est une application desktop de surveillance et de sécurité réseau développée en Java. Elle permet la capture de paquets en temps réel via l'interface réseau, l'application de règles de filtrage dynamiques et la visualisation du trafic.

### ✨ Fonctionnalités Principales

- 🔍 **Capture de Paquets en Temps Réel** - Inspection du trafic réseau avec Pcap4J
- 🚫 **Règles de Filtrage Dynamiques** - Blocage par IP, Port, Protocole, ou Taille de paquet
- 📊 **Visualisation Interactive** - Dashboard JavaFX moderne avec tableaux en temps réel
- 💾 **Persistance des Données** - Stockage dans MySQL via Hibernate/JPA
- 📈 **Historique des Captures** - Consultation et analyse des sessions passées
- 🎯 **System de Priorités** - Évaluation des règles par ordre de priorité
- 📁 **Export CSV** - Exportation des données capturées

## 🏗️ Architecture

```
firewall_security/
├── src/main/java/com/firewall/
│   ├── MainApp.java                    # Point d'entrée de l'application
│   ├── ui/controller/                  # Contrôleurs JavaFX (MVC)
│   │   ├── InterfaceController.java
│   │   └── CaptureController.java
│   ├── capture/                        # Module de capture réseau
│   │   └── PacketCapture.java
│   ├── domain/                         # Objets métier (DTO)
│   │   ├── Packet.java
│   │   └── ProtocolType.java
│   ├── logic/                          # Moteur de règles
│   │   ├── FirewallEngine.java
│   │   └── rules/
│   │       ├── IPBlockRule.java
│   │       ├── PortBlockRule.java
│   │       ├── ProtocolBlockRule.java
│   │       └── PayloadSizeRule.java
│   └── persistence/                    # Couche de persistance
│       ├── PacketRepository.java
│       └── entity/CapturedPacket.java
├── src/main/resources/
│   ├── fxml/                          # Fichiers FXML (Vues)
│   │   ├── MainView.fxml
│   │   └── CaptureView.fxml
│   └── META-INF/persistence.xml       # Configuration Hibernate
├── docker-compose.yml                  # Configuration MySQL Docker
└── pom.xml                            # Dépendances Maven
```

## 🚀 Technologies Utilisées

| Technologie | Usage |
|------------|-------|
| **Java 17+** | Langage principal avec POO avancée |
| **JavaFX** | Interface utilisateur graphique (GUI) |
| **Pcap4J** | Bibliothèque de capture de paquets réseau |
| **Hibernate (JPA)** | ORM pour la persistance des données |
| **MySQL 8.0** | Base de données relationnelle |
| **Docker** | Conteneurisation de la base de données |
| **Maven** | Gestion des dépendances et build |
| **Lombok** | Réduction du code boilerplate |

## 📋 Prérequis

Avant de lancer l'application, assurez-vous d'avoir installé :

- ✅ **Java JDK 17+** ([Télécharger](https://www.oracle.com/java/technologies/downloads/))
- ✅ **Maven 3.8+** ([Télécharger](https://maven.apache.org/download.cgi))
- ✅ **Docker Desktop** ([Télécharger](https://www.docker.com/products/docker-desktop))
- ✅ **Npcap** (pour Windows) ([Télécharger](https://npcap.com/#download))
- ✅ **IntelliJ IDEA** (recommandé) ou tout IDE Java

> ⚠️ **Important** : L'application doit être exécutée avec des **privilèges administrateur** pour capturer les paquets réseau.

## 🔧 Installation

### 1. Cloner le projet

```bash
git clone https://github.com/votre-username/Firewall-Security.git
cd Firewall-Security
```

### 2. Installer Npcap (Windows uniquement)

1. Téléchargez Npcap depuis [https://npcap.com](https://npcap.com/#download)
2. Installez-le avec l'option **"WinPcap API-compatible Mode"** cochée

### 3. Démarrer la base de données MySQL (Docker)

```bash
docker-compose up -d
```

Vérifiez que le conteneur est actif :

```bash
docker ps
```

### 4. Compiler le projet

```bash
mvn clean install
```

### 5. Exécuter l'application

#### Avec Maven (en tant qu'administrateur)

```bash
mvn javafx:run
```

#### Avec IntelliJ IDEA

1. Ouvrez le projet dans IntelliJ
2. Cliquez droit sur `MainApp.java` → **Run As Administrator**

## 🎮 Guide d'Utilisation

### 1. Sélection de l'interface réseau

1. Cliquez sur **"Scan"** pour détecter les interfaces réseau disponibles
2. Sélectionnez votre interface active (Wi-Fi, Ethernet, etc.)
3. Cliquez sur **"Select Interface"**

### 2. Configuration des règles de filtrage

Dans l'onglet **Live Capture**, ajoutez des règles de blocage :

- **Bloquer une IP** : `192.168.1.100`
- **Bloquer un port** : `80` (HTTP)
- **Bloquer un protocole** : `TCP`, `UDP`, ou `ICMP`
- **Bloquer une plage IP** : `192.168.1.0/24` (notation CIDR)
- **Bloquer les gros paquets** : `> 1500 bytes`

### 3. Démarrer la capture

1. Définissez le nombre maximum de paquets (par défaut : 100)
2. Cliquez sur **"▶ Start"**
3. Les paquets s'affichent en temps réel :
   - 🟢 **ALLOWED** : Paquet autorisé
   - 🔴 **BLOCKED** : Paquet bloqué par une règle

### 4. Arrêter et sauvegarder

1. Cliquez sur **"⬛ Stop"**
2. Les paquets sont automatiquement sauvegardés dans MySQL
3. Consultez l'historique dans l'onglet **"Packet History"**

### 5. Exporter les données

- Cliquez sur **"Export CSV"** pour sauvegarder les résultats

## 📊 Captures d'Écran

### Interface de sélection réseau
*(Page d'accueil avec scan des interfaces)*

### Tableau de capture en temps réel
*(Tableau avec paquets ALLOWED/BLOCKED)*

### Historique des paquets
*(Vue base de données)*

## 🗄️ Configuration de la Base de Données

Le fichier `docker-compose.yml` configure automatiquement MySQL :

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: firewall123
      MYSQL_DATABASE: firewall_db
    ports:
      - "3306:3306"
```

### Connexion MySQL

- **Host** : `localhost:3306`
- **Database** : `firewall_db`
- **User** : `root`
- **Password** : `firewall123`

La table `captured_packets` est créée automatiquement par Hibernate au démarrage.

## 🔐 Règles de Sécurité

### Hiérarchie des Priorités

| Priorité | Type de Règle | Exemple |
|----------|--------------|---------|
| 100 | IPBlockRule | Bloquer `192.168.1.100` |
| 95 | IPRangeBlockRule | Bloquer `10.0.0.0/8` |
| 90 | PortBlockRule | Bloquer port `22` (SSH) |
| 85 | PortRangeBlockRule | Bloquer ports `1-1024` |
| 80 | ProtocolBlockRule | Bloquer tout `ICMP` |
| 75 | PayloadSizeRule | Bloquer paquets `> 1500 bytes` |

> 💡 **Note** : Les règles sont évaluées dans l'ordre décroissant de priorité. La première règle correspondante bloque le paquet.

## 🧪 Tests

### Tester la capture

1. Démarrez la capture
2. Ouvrez un navigateur et naviguez sur un site web
3. Vous devriez voir des paquets HTTP/HTTPS apparaître

### Tester les règles de blocage

1. Ajoutez une règle de blocage du port 80
2. Démarrez la capture
3. Naviguez sur un site HTTP (non HTTPS)
4. Les paquets vers le port 80 doivent apparaître comme **BLOCKED**

## 📝 Structure du Code

### Patron de Conception Utilisés

- **MVC** (Model-View-Controller) : Séparation UI/Logique
- **Repository Pattern** : Abstraction de la couche de données
- **Strategy Pattern** : Implémentation des règles de filtrage
- **Observer Pattern** : Mise à jour UI en temps réel (JavaFX Properties)

### Concepts Java Avancés

- ✅ Programmation Orientée Objet (POO)
- ✅ Multithreading (capture réseau en arrière-plan)
- ✅ Gestion de la concurrence (Collections thread-safe)
- ✅ Persistance avec JPA/Hibernate
- ✅ Lambda expressions et Streams
- ✅ Records Java (PacketRow, HistoryRow)
- ✅ Pattern Matching (switch expressions)

## 🐛 Dépannage

### Erreur "Npcap not found"
- **Solution** : Installez Npcap et redémarrez votre ordinateur

### Erreur "Access denied"
- **Solution** : Exécutez l'application en tant qu'administrateur

### Base de données non connectée
- **Solution** : Vérifiez que Docker est lancé avec `docker ps`

### Aucune interface détectée
- **Solution** : Vérifiez que Npcap est installé et que vous êtes administrateur

## 🤝 Contributions

Les contributions sont les bienvenues ! N'hésitez pas à :

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est un projet académique développé dans le cadre du cours **Java Avancé**.

## 👤 Auteur

**YOUNES CHEMMAOUI**
- Email: youneschemmaoui1@gmail.com
- Projet: Firewall Security - Network Packet Analyzer

## 🙏 Remerciements

- **Pcap4J** pour la bibliothèque de capture réseau
- **JavaFX** pour le framework UI moderne
- **Hibernate** pour l'ORM puissant
- **Npcap** pour le driver de capture Windows

---

⭐ Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile !
