package edu.nyu.cs9053.reservo.server.model;

import java.time.LocalDateTime;

public class Resource {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer slotDurationMinutes;
    private Integer bookingHorizonDays;
    private Integer maxHoursPerDay;
    private String rulesJson;
    private LocalDateTime createdAt;

    public Resource() {}

    public Resource(String name, Integer capacity, Integer slotDurationMinutes, 
                   Integer bookingHorizonDays, Integer maxHoursPerDay, String rulesJson) {
        this.name = name;
        this.capacity = capacity;
        this.slotDurationMinutes = slotDurationMinutes;
        this.bookingHorizonDays = bookingHorizonDays;
        this.maxHoursPerDay = maxHoursPerDay;
        this.rulesJson = rulesJson;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getSlotDurationMinutes() { return slotDurationMinutes; }
    public void setSlotDurationMinutes(Integer slotDurationMinutes) { 
        this.slotDurationMinutes = slotDurationMinutes; 
    }

    public Integer getBookingHorizonDays() { return bookingHorizonDays; }
    public void setBookingHorizonDays(Integer bookingHorizonDays) { 
        this.bookingHorizonDays = bookingHorizonDays; 
    }

    public Integer getMaxHoursPerDay() { return maxHoursPerDay; }
    public void setMaxHoursPerDay(Integer maxHoursPerDay) { 
        this.maxHoursPerDay = maxHoursPerDay; 
    }

    public String getRulesJson() { return rulesJson; }
    public void setRulesJson(String rulesJson) { this.rulesJson = rulesJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

