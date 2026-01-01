package com.monitoring.server;

import com.monitoring.server.receiver.TCPAlertReceiver;
import com.monitoring.server.rmi.MonitoringServiceImpl;
import com.monitoring.server.storage.DataManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Point d'entrée principal du serveur de monitoring
 */
public class ServerMain {
    
    // Ports de configuration
    private static final int RMI_PORT = 1099;
    private static final int TCP_ALERT_PORT = 9877;
    private static final String RMI_SERVICE_NAME = "MonitoringService";
    
    // Composants du serveur
    private static DataManager dataManager;
    private static TCPAlertReceiver tcpAlertReceiver;
    private static MonitoringServiceImpl rmiService;
    private static Thread tcpThread;
    
    public static void main(String[] args) {
        System.out.println("=== DÉMARRAGE DU SERVEUR DE MONITORING ===");
        
        try {
            // 1. Initialiser le gestionnaire de données
            System.out.println("[1/4] Initialisation du DataManager...");
            dataManager = new DataManager();
            
            // 2. Démarrer le récepteur TCP pour les alertes
            System.out.println("[2/4] Démarrage du TCP Alert Receiver...");
            tcpAlertReceiver = new TCPAlertReceiver(TCP_ALERT_PORT, dataManager);
            tcpThread = new Thread(tcpAlertReceiver);
            tcpThread.start();
            
            // 3. Démarrer le service RMI
            System.out.println("[3/4] Démarrage du service RMI...");
            startRMIService();
            
            // 4. Le serveur est prêt
            System.out.println("[4/4] Serveur démarré avec succès !");
            System.out.println("\n=== SERVEUR EN ÉCOUTE ===");
            System.out.println("• RMI sur le port: " + RMI_PORT);
            System.out.println("• TCP Alert sur le port: " + TCP_ALERT_PORT);
            System.out.println("• Service RMI disponible sous le nom: " + RMI_SERVICE_NAME);
            System.out.println("\nAppuyez sur Ctrl+C pour arrêter le serveur...\n");
            
            // Garder le serveur actif
            keepServerRunning();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }
    
    /**
     * Démarre le service RMI
     */
    private static void startRMIService() throws Exception {
        // Créer le service RMI
        rmiService = new MonitoringServiceImpl(dataManager);
        
        // Créer ou récupérer le registre RMI
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("  Registre RMI créé sur le port " + RMI_PORT);
        } catch (Exception e) {
            // Si le registre existe déjà, le récupérer
            registry = LocateRegistry.getRegistry(RMI_PORT);
            System.out.println("  Registre RMI existant récupéré sur le port " + RMI_PORT);
        }
        
        // Enregistrer le service dans le registre
        registry.rebind(RMI_SERVICE_NAME, rmiService);
        System.out.println("  Service RMI enregistré sous le nom: " + RMI_SERVICE_NAME);
    }
    
    /**
     * Garde le serveur en cours d'exécution
     */
    private static void keepServerRunning() {
        try {
            // Attendre que le thread TCP soit actif
            Thread.sleep(1000);
            
            // Afficher l'état périodiquement
            while (true) {
                Thread.sleep(30000); // 30 secondes
                System.out.println("\n[STATUT SERVEUR]");
                System.out.println("• TCP Alert Receiver: " + 
                    (tcpAlertReceiver.isRunning() ? "ACTIF" : "INACTIF"));
                System.out.println("• Alertes reçues: " + dataManager.getAlertCount());
                System.out.println("• En attente de connexions...");
            }
            
        } catch (InterruptedException e) {
            System.out.println("\nServeur interrompu");
        }
    }
    
    /**
     * Arrête proprement le serveur
     */
    private static void shutdown() {
        System.out.println("\n=== ARRÊT DU SERVEUR ===");
        
        // Arrêter le récepteur TCP
        if (tcpAlertReceiver != null) {
            System.out.println("[1/2] Arrêt du TCP Alert Receiver...");
            tcpAlertReceiver.stop();
        }
        
        // Arrêter le thread TCP
        if (tcpThread != null && tcpThread.isAlive()) {
            try {
                tcpThread.join(2000); // Attendre 2 secondes
            } catch (InterruptedException e) {
                System.err.println("Erreur lors de l'arrêt du thread TCP");
            }
        }
        
        // Nettoyer le service RMI
        System.out.println("[2/2] Nettoyage du service RMI...");
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_PORT);
            registry.unbind(RMI_SERVICE_NAME);
            System.out.println("  Service RMI désenregistré");
        } catch (Exception e) {
            System.err.println("  Erreur lors du désenregistrement RMI: " + e.getMessage());
        }
        
        System.out.println("=== SERVEUR ARRÊTÉ ===");
    }
    
    /**
     * Ajoute un gestionnaire d'arrêt pour Ctrl+C
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nSignal d'arrêt reçu...");
            shutdown();
        }));
    }
}