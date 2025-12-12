package edu.nyu.cs9053.reservo.server.dto;

public class AuthResponse {
    private Long userId;
    private String username;
    private Boolean isAdmin;
    private String token;

    public AuthResponse() {}

    public AuthResponse(Long userId, String username, Boolean isAdmin, String token) {
        this.userId = userId;
        this.username = username;
        this.isAdmin = isAdmin;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

