package com.monitoring.server.rmi;

import com.monitoring.server.model.Agent;
import com.monitoring.server.model.Alert;
import com.monitoring.server.model.SystemMetrics;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface RMI pour les services de monitoring
 * Les clients pourront appeler ces méthodes à distance
 */
public interface MonitoringService extends Remote {
    
    /**
     * Récupère la liste de tous les agents connectés
     * @return Liste des agents
     * @throws RemoteException
     */
    List<Agent> getAllAgents() throws RemoteException;
    
    /**
     * Récupère la liste de toutes les alertes
     * @return Liste des alertes
     * @throws RemoteException
     */
    List<Alert> getAllAlerts() throws RemoteException;
    
    /**
     * Récupère l'historique des métriques d'un agent spécifique
     * @param agentId Identifiant de l'agent
     * @return Historique des métriques
     * @throws RemoteException
     */
    List<SystemMetrics> getMetricsHistory(String agentId) throws RemoteException;
    
    /**
     * Récupère un agent spécifique par son ID
     * @param agentId Identifiant de l'agent
     * @return L'agent correspondant, ou null si non trouvé
     * @throws RemoteException
     */
    Agent getAgent(String agentId) throws RemoteException;
    
    /**
     * Vérifie si le serveur est en ligne
     * @return Message de statut
     * @throws RemoteException
     */
    String ping() throws RemoteException;
    
    /**
     * Récupère le nombre d'agents connectés
     * @return Nombre d'agents
     * @throws RemoteException
     */
    int getAgentCount() throws RemoteException;
    
    /**
     * Récupère le nombre d'alertes actives
     * @return Nombre d'alertes
     * @throws RemoteException
     */
    int getAlertCount() throws RemoteException;
}