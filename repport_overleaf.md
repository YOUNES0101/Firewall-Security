\documentclass[12pt,a4paper]{report}

% ===========================
% PACKAGES
% ===========================
\usepackage[utf8]{inputenc}
\usepackage[T1]{fontenc}
\usepackage[french]{babel}
\usepackage{geometry}
\usepackage{graphicx}
\usepackage{hyperref}
\usepackage{setspace}
\usepackage{fancyhdr}
\usepackage{titlesec}
\usepackage{xcolor}
\usepackage{listings}
\usepackage{float}
\usepackage{array}
\usepackage{booktabs}
\usepackage{caption}

% ===========================
% PAGE LAYOUT
% ===========================
\geometry{
  left=2.5cm,
  right=2.5cm,
  top=2.5cm,
  bottom=2.5cm
}
\onehalfspacing

% ===========================
% COLORS
% ===========================
\definecolor{emsiblue}{RGB}{0,51,102}
\definecolor{codebg}{RGB}{245,245,245}

% ===========================
% HEADER / FOOTER
% ===========================
\pagestyle{fancy}
\fancyhf{}
\fancyhead[L]{Rapport de Projet Java Avancé}
\fancyhead[R]{4IIR EMSI 2025--2026}
\fancyfoot[C]{\thepage}
\renewcommand{\headrulewidth}{0.4pt}

% ===========================
% HYPERREF
% ===========================
\hypersetup{
  colorlinks=true,
  linkcolor=emsiblue,
  urlcolor=blue,
  pdftitle={Simulateur de Pare-feu (Firewall Simulator)},
  pdfauthor={YOUNES CHEMMAOUI}
}

% ===========================
% TITLES
% ===========================
\titleformat{\chapter}[display]
  {\normalfont\huge\bfseries\color{emsiblue}}
  {\chaptername\ \thechapter}{20pt}{\Huge}

% ===========================
% CODE LISTINGS (JAVA)
% ===========================
\lstdefinestyle{javastyle}{
  language=Java,
  backgroundcolor=\color{codebg},
  basicstyle=\ttfamily\footnotesize,
  keywordstyle=\color{blue}\bfseries,
  commentstyle=\color{gray},
  stringstyle=\color{red!70!black},
  numbers=left,
  numberstyle=\tiny\color{gray},
  stepnumber=1,
  numbersep=8pt,
  tabsize=4,
  showspaces=false,
  showstringspaces=false,
  frame=single,
  breaklines=true
}
\lstset{style=javastyle}

\begin{document}

% ==========================================
% 1. PAGE DE GARDE
% ==========================================
\begin{titlepage}
  \centering

  % ===== LOGO EN HAUT =====
  % Assurez-vous que EMSILOGO.png est présent dans le projet Overleaf
  \includegraphics[width=\textwidth]{EMSILOGO.png}\\[1.5cm]

  {\large \textbf{ÉCOLE MAROCAINE DES SCIENCES DE L'INGÉNIEUR}}\\[0.3cm]
  {\large \textbf{EMSI}}\\[1.5cm]

  {\Large 4IIR -- 2025--2026}\\[1cm]

  \rule{\linewidth}{0.5mm}\\[0.4cm]
  {\Huge \textbf{Simulateur de Pare-feu}}\\[0.2cm]
  {\Large (Firewall Simulator)}\\[0.4cm]
  \rule{\linewidth}{0.5mm}\\[1.5cm]

  {\Large \textbf{Rapport de Projet Java Avancé}}\\[0.3cm]
  {\large Module : Java Avancé / Programmation Orientée Objet}\\[2cm]

  \begin{flushleft}
    {\large \textbf{Réalisé par :}}\\[0.2cm]
    {\large YOUNES CHEMMAOUI}\\[0.8cm]

    {\large \textbf{Encadré par :}}\\[0.2cm]
    {\large Pr. Abderrahim Larhlimi}\\[1.5cm]

    {\large \textbf{Année Universitaire :} 2025--2026}
  \end{flushleft}

  \vfill
\end{titlepage}

% ==========================================
% 2. REMERCIEMENTS (OPTIONNEL)
% ==========================================
\chapter*{Remerciements}
\addcontentsline{toc}{chapter}{Remerciements}

Je tiens à remercier sincèrement mon encadrant, \textbf{Pr. Abderrahim Larhlimi}, pour son accompagnement, ses conseils techniques et pédagogiques, ainsi que pour la confiance accordée durant la réalisation de ce projet.

Je remercie également l'administration de l'EMSI, ainsi que toutes les personnes qui ont contribué, de près ou de loin, à la bonne conduite de ce travail.

\newpage

% ==========================================
% 3. TABLE DES MATIÈRES
% ==========================================
\tableofcontents
\newpage

% ==========================================
% 4. INTRODUCTION GÉNÉRALE
% ==========================================
\chapter{Introduction Générale}

\section{Contexte du projet}
Dans un environnement réseau moderne, les infrastructures informatiques doivent traiter un volume très important de requêtes et de connexions. La sécurité repose en grande partie sur la capacité à \textbf{analyser rapidement} ce trafic, à \textbf{identifier les menaces potentielles} et à \textbf{bloquer les accès non autorisés}.

Les pare-feux (firewalls) jouent un rôle central dans cette protection : ils agissent comme des filtres intelligents situés entre le réseau interne et l'extérieur, en appliquant un ensemble de règles de sécurité.

\section{Problématique}
Face à la croissance du trafic réseau et à la diversité des attaques (scans de ports, tentatives d'intrusion, abus de protocoles, etc.), les systèmes de filtrage doivent :

\begin{itemize}
  \item traiter un \textbf{grand nombre de paquets} en un temps réduit ;
  \item appliquer des \textbf{règles de sécurité complexes} (adresses IP, ports, protocoles, états de connexion, etc.) ;
  \item rester \textbf{flexibles} pour s'adapter à de nouveaux scénarios d'attaque.
\end{itemize}

La question centrale est donc : \textit{comment concevoir un simulateur de pare-feu en Java capable de modéliser un trafic réseau et d'appliquer efficacement des règles de filtrage ?}

\section{Objectifs}
L'objectif principal de ce projet est de développer une application Java simulant le comportement d'un pare-feu. Plus précisément :

\begin{itemize}
  \item Générer un \textbf{flux massif de paquets réseau} simulés (adresse IP source, port destination, protocole, etc.).
  \item Mettre en place un \textbf{moteur de règles} permettant de décider pour chaque paquet : \textit{Allow}, \textit{Block} ou \textit{Drop}.
  \item Permettre la \textbf{configuration dynamique} des règles (ajout, suppression, modification).
  \item Fournir une \textbf{interface utilisateur} affichant en temps réel l'état du trafic simulé et les décisions prises.
  \item Structurer le projet selon une \textbf{architecture claire} et des \textbf{design patterns} adaptés.
\end{itemize}

% ==========================================
% 5. PARTIE I : ANALYSE ET CONCEPTION
% ==========================================
\chapter{Partie I : Analyse et Conception}

\section{Spécification des besoins}

\subsection{Besoins fonctionnels}
Les principaux besoins fonctionnels du simulateur de pare-feu sont :

\begin{itemize}
  \item BF01 : Générer un flux de \textbf{paquets réseau} simulés.
  \item BF02 : Afficher les informations d'un paquet (IP source, IP destination, port, protocole, état, etc.).
  \item BF03 : Définir et gérer une liste de \textbf{règles de filtrage} (conditions + action).
  \item BF04 : Appliquer les règles sur chaque paquet et décider : \textit{Allow}, \textit{Block} ou \textit{Drop}.
  \item BF05 : Afficher, en temps réel, le résultat du filtrage (journal des décisions).
  \item BF06 : Permettre l’\textbf{activation / désactivation} d’une règle.
  \item BF07 : Fournir des \textbf{statistiques} simples (nombre de paquets autorisés, bloqués, rejetés).
\end{itemize}

\subsection{Besoins non fonctionnels}
\begin{itemize}
  \item \textbf{Performance :} traitement rapide d'un grand nombre de paquets simulés.
  \item \textbf{Ergonomie :} interface simple, claire, permettant de visualiser rapidement l'état du pare-feu.
  \item \textbf{Extensibilité :} facilité d'ajouter de nouveaux types de règles ou de nouveaux protocoles.
  \item \textbf{Maintenabilité :} code structuré, utilisation de design patterns pour limiter le couplage.
  \item \textbf{Portabilité :} application Java compatible multi-plateforme.
\end{itemize}

\section{Conception UML}

\subsection{Diagramme de cas d'utilisation (Use Case)}
Les acteurs principaux sont :
\begin{itemize}
  \item \textbf{Administrateur / Utilisateur} : configure les règles et observe le trafic.
\end{itemize}

\begin{figure}[H]
  \centering
  % \includegraphics[width=0.8\textwidth]{use_case_firewall.png}
  \fbox{\parbox{0.8\textwidth}{\centering\textit{Insérer ici le diagramme de cas d'utilisation du simulateur de pare-feu}}}
  \caption{Diagramme de cas d'utilisation du simulateur de pare-feu}
\end{figure}

\subsection{Diagramme de classes}
La structure conceptuelle du simulateur s'articule autour des classes principales suivantes :

\begin{itemize}
  \item \texttt{Packet} : représente un paquet réseau (IP source, IP destination, port, protocole, etc.).
  \item \texttt{FirewallRule} : représente une règle (condition + action).
  \item \texttt{RuleEngine} : moteur de règles qui applique les règles à un paquet.
  \item \texttt{TrafficGenerator} : génère des paquets simulés.
  \item \texttt{FirewallSimulator} : point central orchestrant la génération et le filtrage.
  \item \texttt{Logger} / \texttt{AuditService} : journalisation des décisions.
\end{itemize}

\begin{figure}[H]
  \centering
  % \includegraphics[width=\textwidth]{uml_firewall.png}
  \fbox{\parbox{0.9\textwidth}{\centering\textit{Insérer ici le diagramme de classes du simulateur de pare-feu}}}
  \caption{Diagramme de classes conceptuel du simulateur de pare-feu}
\end{figure}

\subsection{Diagramme de séquence (optionnel)}
Un scénario important est le \textbf{traitement d'un paquet} :

\begin{figure}[H]
  \centering
  % \includegraphics[width=0.9\textwidth]{sequence_firewall.png}
  \fbox{\parbox{0.9\textwidth}{\centering\textit{Insérer ici le diagramme de séquence : génération et filtrage d'un paquet}}}
  \caption{Diagramme de séquence du traitement d'un paquet}
\end{figure}

\section{Conception de la base de données}
Dans ce projet, la persistance peut être limitée (journalisation en mémoire ou en fichier). Si une base est utilisée pour stocker les logs ou les règles :

\subsection{Modèle logique de données (MLD)}
\begin{figure}[H]
  \centering
  % \includegraphics[width=0.8\textwidth]{mld_firewall.png}
  \fbox{\parbox{0.8\textwidth}{\centering\textit{Insérer ici le MLD (tables : rules, logs, etc.)}}}
  \caption{Modèle logique de données du simulateur}
\end{figure}

\subsection{Dictionnaire de données}
\begin{table}[H]
  \centering
  \caption{Exemple de dictionnaire de données pour la table \texttt{firewall\_rule}}
  \begin{tabular}{|l|l|l|l|}
    \hline
    \textbf{Champ} & \textbf{Type} & \textbf{Taille} & \textbf{Contrainte} \\
    \hline
    id & INT & -- & PK, AUTO\_INCREMENT \\
    \hline
    source\_ip & VARCHAR & 45 & NULL possible \\
    \hline
    dest\_port & INT & -- & NULL possible \\
    \hline
    protocol & VARCHAR & 10 & NULL possible (TCP/UDP/ANY) \\
    \hline
    action & VARCHAR & 10 & NOT NULL (ALLOW/BLOCK/DROP) \\
    \hline
    enabled & BOOLEAN & -- & NOT NULL \\
    \hline
  \end{tabular}
\end{table}

% ==========================================
% 6. PARTIE II : ENVIRONNEMENT TECHNIQUE
% ==========================================
\chapter{Partie II : Environnement Technique}

\section{Langage de programmation}
Le projet est développé en \textbf{Java 21}. Ce choix permet de bénéficier des dernières fonctionnalités du langage tout en conservant la portabilité et la robustesse propres à l'écosystème Java.

\section{Environnement de développement (IDE)}
L’implémentation a été réalisée sous \textbf{IntelliJ IDEA} (ou autre, à adapter) qui offre une excellente intégration avec Maven, un débogueur puissant et un support avancé pour les projets Java.

\section{Gestion de projet / Build}
Le gestionnaire de build utilisé est \textbf{Maven}, afin d’automatiser la gestion des dépendances et la compilation.  
Exemple d’extrait du fichier \texttt{pom.xml} :

\begin{lstlisting}[language=XML,caption={Exemple de dépendance Maven}]
<dependencies>
    <!-- Exemple : journaling, tests, etc. -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.13</version>
    </dependency>
</dependencies>
\end{lstlisting}

\section{Système de gestion de base de données (SGBD)}
Selon la version choisie, les logs peuvent être stockés :
\begin{itemize}
  \item soit en mémoire ou dans des fichiers texte,
  \item soit dans un SGBD comme \textbf{MySQL 8.0} si une persistance longue durée est nécessaire.
\end{itemize}

\section{Outils de modélisation}
Pour la modélisation, des outils comme \textbf{StarUML} ou \textbf{Draw.io} peuvent être utilisés pour dessiner les diagrammes UML (use case, classes, séquence).

\section{Bibliothèques tierces}
\begin{itemize}
  \item Bibliothèques de logging (par exemple SLF4J).
  \item Éventuellement bibliothèques de tests (JUnit) pour la validation du moteur de règles.
\end{itemize}

% ==========================================
% 7. PARTIE III : ARCHITECTURE ET IMPLÉMENTATION
% ==========================================
\chapter{Partie III : Architecture et Implémentation}

\section{Architecture logicielle}
L’application suit une architecture en couches inspirée du modèle MVC :

\begin{itemize}
  \item \texttt{ma.emsi.model} : représentation des entités (\texttt{Packet}, \texttt{FirewallRule}, etc.).
  \item \texttt{ma.emsi.service} : logique métier (génération de trafic, application des règles).
  \item \texttt{ma.emsi.ui} : interface utilisateur (console ou graphique).
  \item \texttt{ma.emsi.util} : classes utilitaires (validation, logs, etc.).
\end{itemize}

\section{Design patterns (patrons de conception)}

\subsection{Singleton}
Un \textbf{Singleton} peut être utilisé pour le composant central \texttt{FirewallSimulator} ou pour un service de configuration partagé.

\subsection{Strategy / Chain of Responsibility}
Le moteur de règles peut utiliser un pattern \textbf{Strategy} ou \textbf{Chain of Responsibility} pour appliquer une série de règles sur un paquet, chacune pouvant décider de le bloquer, l'autoriser ou le laisser passer à la règle suivante.

\subsection{DAO (si persistance)}
Si une base de données est utilisée pour stocker les règles ou les logs, le pattern \textbf{DAO} permet d’isoler la couche d’accès aux données.

\section{Extraits de code clés}

\subsection{Représentation d’un paquet réseau}
\begin{lstlisting}[caption={Classe Packet (exemple simplifié)}]
public class Packet {
    private final String sourceIp;
    private final String destinationIp;
    private final int destinationPort;
    private final String protocol; // TCP, UDP, ...

    public Packet(String sourceIp, String destinationIp,
                  int destinationPort, String protocol) {
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
    }

    // getters ...
}
\end{lstlisting}

\subsection{Représentation d’une règle de pare-feu}
\begin{lstlisting}[caption={Classe FirewallRule (exemple)}]
public enum Action {
    ALLOW, BLOCK, DROP
}

public class FirewallRule {
    private final String sourceIp;
    private final Integer destinationPort;
    private final String protocol;
    private final Action action;
    private boolean enabled = true;

    public boolean matches(Packet packet) {
        // Exemple de logique de matching simplifiée
        if (sourceIp != null && !sourceIp.equals(packet.getSourceIp())) return false;
        if (destinationPort != null && destinationPort != packet.getDestinationPort()) return false;
        if (protocol != null && !protocol.equalsIgnoreCase(packet.getProtocol())) return false;
        return true;
    }

    // getters / setters ...
}
\end{lstlisting}

\subsection{Moteur de règles}
\begin{lstlisting}[caption={Moteur de règles}]
public class RuleEngine {

    private final List<FirewallRule> rules;

    public RuleEngine(List<FirewallRule> rules) {
        this.rules = rules;
    }

    public Action decide(Packet packet) {
        for (FirewallRule rule : rules) {
            if (rule.isEnabled() && rule.matches(packet)) {
                return rule.getAction();
            }
        }
        // Politique par défaut : DROP par prudence
        return Action.DROP;
    }
}
\end{lstlisting}

% ==========================================
% 8. PARTIE IV : INTERFACE UTILISATEUR ET TESTS
% ==========================================
\chapter{Partie IV : Interface Utilisateur et Tests}

\section{Présentation des interfaces (captures d'écran)}
Insérer ici des captures :
\begin{itemize}
  \item fenêtre principale du simulateur,
  \item affichage du flux de paquets,
  \item écran de gestion des règles.
\end{itemize}

\begin{figure}[H]
  \centering
  % \includegraphics[width=0.8\textwidth]{ui_main.png}
  \fbox{\parbox{0.8\textwidth}{\centering\textit{Insérer ici la capture d'écran de l'interface principale}}}
  \caption{Interface principale du simulateur de pare-feu}
\end{figure}

\section{Scénarios de test}

\subsection{Tests nominaux}
\begin{itemize}
  \item TN01 : génération de paquets avec des adresses IP et ports valides, application correcte des règles définies.
  \item TN02 : ajout d’une nouvelle règle et vérification immédiate de son effet sur le trafic.
  \item TN03 : désactivation d’une règle et observation du changement de comportement.
\end{itemize}

\subsection{Tests d’erreurs}
\begin{itemize}
  \item TE01 : paquet avec protocole inconnu \textrightarrow{} vérification de la politique de défaut (DROP).
  \item TE02 : règle mal configurée (ex : port négatif) \textrightarrow{} message d’erreur ou rejet de la configuration.
  \item TE03 : charge importante (grand nombre de paquets) \textrightarrow{} vérification de la stabilité et du temps de réponse.
\end{itemize}

% ==========================================
% 9. CONCLUSION ET PERSPECTIVES
% ==========================================
\chapter{Conclusion et Perspectives}

\section{Bilan technique}
Le projet \textit{Simulateur de Pare-feu} a permis de mettre en place :
\begin{itemize}
  \item un modèle de \textbf{paquets réseau} et de \textbf{règles de filtrage},
  \item un \textbf{moteur de règles} capable de décider pour chaque paquet,
  \item une \textbf{interface} permettant de visualiser le trafic et les décisions,
  \item une architecture Java claire et extensible.
\end{itemize}

\section{Bilan personnel}
Ce projet m’a permis de renforcer mes compétences en :
\begin{itemize}
  \item conception orientée objet et modélisation UML ;
  \item programmation Java avancée (collections, énumérations, exceptions, etc.) ;
  \item réflexion sur la \textbf{sécurité réseau} et les mécanismes de filtrage ;
  \item organisation du code en couches et réutilisabilité.
\end{itemize}

\section{Difficultés rencontrées}
Parmi les principales difficultés :
\begin{itemize}
  \item trouver un \textbf{bon compromis} entre simplicité du modèle et réalisme du simulateur ;
  \item gérer efficacement le \textbf{grand nombre de paquets} générés pour les tests ;
  \item définir une \textbf{API de règles} flexible, permettant de couvrir plusieurs scénarios.
\end{itemize}

\section{Perspectives et améliorations futures}
\begin{itemize}
  \item Passage à une \textbf{interface graphique avancée} (JavaFX) avec graphiques temps réel.
  \item Ajout de \textbf{statistiques détaillées} (taux de blocage, histogrammes de ports, etc.).
  \item Intégration avec une \textbf{vraie capture de trafic} (ex : PCAP) pour tester sur des données réelles.
  \item Export des logs au format standard (syslog) ou dans une base de données.
\end{itemize}

% ==========================================
% 10. WEBOGRAPHIE / BIBLIOGRAPHIE
% ==========================================
\chapter*{Webographie / Bibliographie}
\addcontentsline{toc}{chapter}{Webographie / Bibliographie}

\begin{itemize}
  \item Documentation officielle Java : \url{https://docs.oracle.com/en/java/}
  \item Articles et tutoriels sur les pare-feux et la sécurité réseau.
  \item Ressources diverses (StackOverflow, blogs techniques) pour certaines implémentations.
\end{itemize}

\appendix
\chapter*{Conseils pour un bon rapport}
\addcontentsline{toc}{chapter}{Conseils pour un bon rapport}

\begin{enumerate}
  \item \textbf{Formatage du code :} utilisez une police à chasse fixe (comme \texttt{listings}) et des extraits de code courts mais significatifs.
  \item \textbf{Cohérence :} assurez-vous que les diagrammes UML correspondent bien aux classes Java réelles.
  \item \textbf{Justification :} expliquez toujours \emph{pourquoi} une technologie ou un pattern a été utilisé (et pas seulement \emph{quoi}).
\end{enumerate}

\end{document}