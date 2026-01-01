package com.monitoring.server.storage;

import com.monitoring.server.model.Alert;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataManager {
    private List<Alert> alerts;
    
    public DataManager() {
        // Utiliser CopyOnWriteArrayList pour la thread-safety
        this.alerts = new CopyOnWriteArrayList<>();
    }
    
    public void addAlert(Alert alert) {
        alerts.add(alert);
        System.out.println("[DataManager] Alerte ajoutée: " + alert);
    }
    
    public List<Alert> getAllAlerts() {
        return new ArrayList<>(alerts);
    }
    
    public int getAlertCount() {
        return alerts.size();
    }
}