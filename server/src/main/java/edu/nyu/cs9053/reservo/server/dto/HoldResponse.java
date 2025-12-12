package edu.nyu.cs9053.reservo.server.dto;

import java.time.LocalDateTime;

public class HoldResponse {
    private Long id;
    private Long timeSlotId;
    private Integer qty;
    private LocalDateTime expiresAt;
    private String requestId;

    public HoldResponse() {}

    public HoldResponse(Long id, Long timeSlotId, Integer qty, 
                       LocalDateTime expiresAt, String requestId) {
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.qty = qty;
        this.expiresAt = expiresAt;
        this.requestId = requestId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}

