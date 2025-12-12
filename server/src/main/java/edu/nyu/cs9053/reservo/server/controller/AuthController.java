package edu.nyu.cs9053.reservo.server.controller;

import edu.nyu.cs9053.reservo.server.dto.AuthRequest;
import edu.nyu.cs9053.reservo.server.dto.AuthResponse;
import edu.nyu.cs9053.reservo.server.model.User;
import edu.nyu.cs9053.reservo.server.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Simple in-memory token store (in production, use Redis or JWT)
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<String> tokenOpt = authService.login(request.getUsername(), request.getPassword());
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = tokenOpt.get();
        Optional<User> userOpt = authService.getUserByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(500).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        tokenStore.put(token, user.getId());

        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getIsAdmin(),
                token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Email is required"));
            }

            User user = authService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );

            String token = UUID.randomUUID().toString();
            tokenStore.put(token, user.getId());

            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getIsAdmin(),
                    token
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
            }

            Optional<User> userOpt = authService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "isAdmin", user.getIsAdmin()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update-email")
    public ResponseEntity<?> updateEmail(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
            }

            String newEmail = request.get("email");
            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Email is required"));
            }

            authService.updateEmail(userId, newEmail);
            return ResponseEntity.ok(Map.of("message", "Email updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
            }

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Both old and new passwords are required"));
            }

            authService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    public Long getUserIdFromToken(String token) {
        if (token == null) {
            return null;
        }
        return tokenStore.get(token);
    }

    public boolean isValidToken(String token) {
        return token != null && tokenStore.containsKey(token);
    }
}

