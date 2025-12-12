package edu.nyu.cs9053.reservo.server.dto;

import java.time.LocalDateTime;

public class HoldRequest {
    private Long timeSlotId;
    private Integer qty;
    private String requestId;

    public HoldRequest() {}

    public HoldRequest(Long timeSlotId, Integer qty, String requestId) {
        this.timeSlotId = timeSlotId;
        this.qty = qty;
        this.requestId = requestId;
    }

    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}

