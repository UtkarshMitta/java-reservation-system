package edu.nyu.cs9053.reservo.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityResponse {
    private Long resourceId;
    private String resourceName;
    private List<SlotAvailability> slots;

    public AvailabilityResponse() {}

    public AvailabilityResponse(Long resourceId, String resourceName, List<SlotAvailability> slots) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.slots = slots;
    }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public List<SlotAvailability> getSlots() { return slots; }
    public void setSlots(List<SlotAvailability> slots) { this.slots = slots; }

    public static class SlotAvailability {
        private Long id;
        private LocalDateTime startTs;
        private LocalDateTime endTs;
        private Integer capacityRemaining;
        private Integer totalCapacity;

        public SlotAvailability() {}

        public SlotAvailability(Long id, LocalDateTime startTs, LocalDateTime endTs, 
                              Integer capacityRemaining, Integer totalCapacity) {
            this.id = id;
            this.startTs = startTs;
            this.endTs = endTs;
            this.capacityRemaining = capacityRemaining;
            this.totalCapacity = totalCapacity;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public LocalDateTime getStartTs() { return startTs; }
        public void setStartTs(LocalDateTime startTs) { this.startTs = startTs; }

        public LocalDateTime getEndTs() { return endTs; }
        public void setEndTs(LocalDateTime endTs) { this.endTs = endTs; }

        public Integer getCapacityRemaining() { return capacityRemaining; }
        public void setCapacityRemaining(Integer capacityRemaining) { 
            this.capacityRemaining = capacityRemaining; 
        }

        public Integer getTotalCapacity() { return totalCapacity; }
        public void setTotalCapacity(Integer totalCapacity) { 
            this.totalCapacity = totalCapacity; 
        }
    }
}

