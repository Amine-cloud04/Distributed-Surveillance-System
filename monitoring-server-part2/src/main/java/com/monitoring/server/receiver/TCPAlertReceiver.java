package com.monitoring.server.receiver;

import com.monitoring.server.model.Alert;
import com.monitoring.server.storage.DataManager;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;

/**
 * Classe qui reçoit les alertes critiques envoyées par les agents via TCP
 */
public class TCPAlertReceiver implements Runnable {
    
    private final int port;
    private final DataManager dataManager;
    private boolean running;
    private ServerSocket serverSocket;
    
    /**
     * Constructeur
     * @param port Port TCP sur lequel écouter (ex: 9877)
     * @param dataManager Gestionnaire de données pour stocker les alertes
     */
    public TCPAlertReceiver(int port, DataManager dataManager) {
        this.port = port;
        this.dataManager = dataManager;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            // Créer le ServerSocket
            serverSocket = new ServerSocket(port);
            System.out.println("[TCP Alert Receiver] En écoute sur le port " + port);
            
            // Accepter les connexions en boucle
            while (running) {
                try {
                    // Attendre une connexion d'un agent
                    Socket clientSocket = serverSocket.accept();
                    
                    // Traiter la connexion dans un thread séparé
                    new Thread(() -> handleClient(clientSocket)).start();
                    
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("[TCP] Erreur de socket: " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("[TCP] Erreur d'E/S: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("[TCP] Impossible de démarrer le serveur sur le port " + port);
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    /**
     * Gère la connexion d'un client (agent)
     */
    private void handleClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        System.out.println("[TCP] Connexion acceptée de " + clientAddress);
        
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Lire le message d'alerte envoyé par l'agent
            String alertMessage = reader.readLine();
            if (alertMessage != null && !alertMessage.isEmpty()) {
                // Extraire les informations du message
                // Format attendu: "AGENT_ID|CPU|85.5|MEMORY|92.3|DISK|78.9"
                String[] parts = alertMessage.split("\\|");
                
                if (parts.length >= 6) {
                    String agentId = parts[0];
                    String metricType = parts[1];
                    double metricValue = Double.parseDouble(parts[2]);
                    String severity = determineSeverity(metricType, metricValue);
                    
                    // Créer l'objet Alert
                    Alert alert = new Alert(
                        agentId,
                        "[" + metricType + "] Dépasse le seuil: " + metricValue + "%",
                        LocalDateTime.now(),
                        severity
                    );
                    
                    // Ajouter l'alerte au DataManager
                    dataManager.addAlert(alert);
                    
                    // Confirmer la réception
                    writer.println("ALERTE_RECUE");
                    
                    System.out.println("[TCP] Alerte reçue de " + agentId + 
                                     ": " + metricType + " = " + metricValue + "%");
                    
                } else {
                    System.err.println("[TCP] Format de message invalide: " + alertMessage);
                }
            }
            
        } catch (IOException e) {
            System.err.println("[TCP] Erreur de communication avec " + clientAddress);
        } catch (NumberFormatException e) {
            System.err.println("[TCP] Format numérique invalide dans le message");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("[TCP] Erreur lors de la fermeture de la socket");
            }
        }
    }
    
    /**
     * Détermine la sévérité de l'alerte en fonction de la métrique
     */
    private String determineSeverity(String metricType, double value) {
        if (value >= 95) {
            return "CRITICAL";
        } else if (value >= 90) {
            return "HIGH";
        } else if (value >= 80) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Arrête le serveur TCP
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("[TCP Alert Receiver] Arrêté");
            } catch (IOException e) {
                System.err.println("[TCP] Erreur lors de la fermeture du ServerSocket");
            }
        }
    }
    
    /**
     * Vérifie si le serveur est en cours d'exécution
     */
    public boolean isRunning() {
        return running;
    }
}