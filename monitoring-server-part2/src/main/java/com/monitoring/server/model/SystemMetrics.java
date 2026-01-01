package com.monitoring.server.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SystemMetrics implements Serializable {
    private String agentId;
    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;
    private LocalDateTime timestamp;
    
    public SystemMetrics(String agentId, double cpuUsage, double memoryUsage, double diskUsage) {
        this.agentId = agentId;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public String getAgentId() { return agentId; }
    public double getCpuUsage() { return cpuUsage; }
    public double getMemoryUsage() { return memoryUsage; }
    public double getDiskUsage() { return diskUsage; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    // Setters
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
    public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
    public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return String.format("SystemMetrics[Agent=%s, CPU=%.1f%%, RAM=%.1f%%, Disk=%.1f%%, Time=%s]",
                agentId, cpuUsage, memoryUsage, diskUsage, timestamp);
    }
}