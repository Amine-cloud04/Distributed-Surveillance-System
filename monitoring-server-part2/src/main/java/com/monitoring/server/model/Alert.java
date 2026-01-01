package com.monitoring.server.model;

import java.time.LocalDateTime;

public class Alert {
    private String agentId;
    private String message;
    private LocalDateTime timestamp;
    private String severity;
    
    public Alert(String agentId, String message, LocalDateTime timestamp, String severity) {
        this.agentId = agentId;
        this.message = message;
        this.timestamp = timestamp;
        this.severity = severity;
    }
    
    // Getters
    public String getAgentId() { return agentId; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSeverity() { return severity; }
    
    // Setters
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    @Override
    public String toString() {
        return "Alert{" +
                "agentId='" + agentId + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", severity='" + severity + '\'' +
                '}';
    }
}