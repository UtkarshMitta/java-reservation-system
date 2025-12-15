package edu.nyu.cs9053.reservo.server.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username; // Plain username for display
    private String usernameHash; // Hashed username for lookup
    private String passwordHash;
    private String email;
    private Boolean isAdmin;
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String username, String usernameHash, String passwordHash, String email, Boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.usernameHash = usernameHash;
        this.passwordHash = passwordHash;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUsernameHash() { return usernameHash; }
    public void setUsernameHash(String usernameHash) { this.usernameHash = usernameHash; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

