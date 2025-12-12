package edu.nyu.cs9053.reservo.server.model;

import java.time.LocalDateTime;

public class TimeSlot {
    private Long id;
    private Long resourceId;
    private LocalDateTime startTs;
    private LocalDateTime endTs;
    private Integer capacityRemaining;
    private Integer version;
    private LocalDateTime createdAt;

    public TimeSlot() {}

    public TimeSlot(Long resourceId, LocalDateTime startTs, LocalDateTime endTs, 
                   Integer capacityRemaining) {
        this.resourceId = resourceId;
        this.startTs = startTs;
        this.endTs = endTs;
        this.capacityRemaining = capacityRemaining;
        this.version = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public LocalDateTime getStartTs() { return startTs; }
    public void setStartTs(LocalDateTime startTs) { this.startTs = startTs; }

    public LocalDateTime getEndTs() { return endTs; }
    public void setEndTs(LocalDateTime endTs) { this.endTs = endTs; }

    public Integer getCapacityRemaining() { return capacityRemaining; }
    public void setCapacityRemaining(Integer capacityRemaining) { 
        this.capacityRemaining = capacityRemaining; 
    }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

