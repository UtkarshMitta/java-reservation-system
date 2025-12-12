package edu.nyu.cs9053.reservo.server.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class WebSocketEvent {
    private String type;
    private LocalDateTime timestamp;
    private Map<String, Object> data;

    public WebSocketEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public WebSocketEvent(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}

