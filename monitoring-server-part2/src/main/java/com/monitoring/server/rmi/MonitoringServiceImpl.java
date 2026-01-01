package com.monitoring.server.rmi;

import com.monitoring.server.model.Agent;
import com.monitoring.server.model.Alert;
import com.monitoring.server.model.SystemMetrics;
import com.monitoring.server.storage.DataManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation du service RMI de monitoring
 */
public class MonitoringServiceImpl extends UnicastRemoteObject implements MonitoringService {
    
    private final DataManager dataManager;
    
    // Stockage simulé des agents (sera remplacé par le vrai DataManager plus tard)
    private final Map<String, Agent> agents;
    private final Map<String, List<SystemMetrics>> metricsHistory;
    
    public MonitoringServiceImpl(DataManager dataManager) throws RemoteException {
        super(); // Important pour RMI
        this.dataManager = dataManager;
        this.agents = new ConcurrentHashMap<>();
        this.metricsHistory = new ConcurrentHashMap<>();
        
        // Ajouter quelques agents de test pour démo
        initializeSampleData();
    }
    
    /**
     * Initialise des données de test pour démonstration
     */
    private void initializeSampleData() {
        // Agent de test 1
        SystemMetrics metrics1 = new SystemMetrics("AGENT-001", 45.2, 67.8, 55.3);
        Agent agent1 = new Agent("AGENT-001", "192.168.1.101", metrics1);
        agents.put("AGENT-001", agent1);
        
        // Historique pour l'agent 1
        List<SystemMetrics> history1 = new ArrayList<>();
        history1.add(new SystemMetrics("AGENT-001", 42.1, 65.2, 54.8));
        history1.add(new SystemMetrics("AGENT-001", 44.3, 66.7, 55.1));
        history1.add(new SystemMetrics("AGENT-001", 45.2, 67.8, 55.3));
        metricsHistory.put("AGENT-001", history1);
        
        // Agent de test 2
        SystemMetrics metrics2 = new SystemMetrics("AGENT-002", 78.9, 82.4, 61.7);
        Agent agent2 = new Agent("AGENT-002", "192.168.1.102", metrics2);
        agent2.setStatus("ALERT");
        agents.put("AGENT-002", agent2);
        
        // Agent de test 3
        SystemMetrics metrics3 = new SystemMetrics("AGENT-003", 23.4, 45.6, 34.2);
        Agent agent3 = new Agent("AGENT-003", "192.168.1.103", metrics3);
        agent3.setStatus("OFFLINE");
        agents.put("AGENT-003", agent3);
    }
    
    @Override
    public List<Agent> getAllAgents() throws RemoteException {
        System.out.println("[RMI] Demande de tous les agents reçue");
        return new ArrayList<>(agents.values());
    }
    
    @Override
    public List<Alert> getAllAlerts() throws RemoteException {
        System.out.println("[RMI] Demande de toutes les alertes reçue");
        return dataManager.getAllAlerts();
    }
    
    @Override
    public List<SystemMetrics> getMetricsHistory(String agentId) throws RemoteException {
        System.out.println("[RMI] Demande d'historique pour l'agent: " + agentId);
        List<SystemMetrics> history = metricsHistory.get(agentId);
        if (history != null) {
            return new ArrayList<>(history);
        } else {
            System.out.println("[RMI] Aucun historique trouvé pour l'agent: " + agentId);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Agent getAgent(String agentId) throws RemoteException {
        System.out.println("[RMI] Demande de l'agent: " + agentId);
        return agents.get(agentId);
    }
    
    @Override
    public String ping() throws RemoteException {
        return "Serveur RMI de monitoring actif - " + 
               agents.size() + " agent(s) - " + 
               dataManager.getAlertCount() + " alerte(s)";
    }
    
    @Override
    public int getAgentCount() throws RemoteException {
        return agents.size();
    }
    
    @Override
    public int getAlertCount() throws RemoteException {
        return dataManager.getAlertCount();
    }
    
    /**
     * Méthode pour ajouter ou mettre à jour un agent
     * (Utilisée par UDPReceiver quand il reçoit des données)
     */
    public void updateAgent(SystemMetrics metrics, String ipAddress) {
        String agentId = metrics.getAgentId();
        
        if (agents.containsKey(agentId)) {
            // Mettre à jour l'agent existant
            Agent agent = agents.get(agentId);
            agent.setLastMetrics(metrics);
            agent.setStatus("ONLINE");
            System.out.println("[RMI] Agent " + agentId + " mis à jour");
        } else {
            // Créer un nouvel agent
            Agent newAgent = new Agent(agentId, ipAddress, metrics);
            agents.put(agentId, newAgent);
            System.out.println("[RMI] Nouvel agent ajouté: " + agentId);
        }
        
        // Ajouter aux historiques
        metricsHistory.computeIfAbsent(agentId, k -> new ArrayList<>())
                     .add(metrics);
        
        // Garder seulement les 50 dernières métriques
        List<SystemMetrics> history = metricsHistory.get(agentId);
        if (history.size() > 50) {
            history.remove(0);
        }
    }
}