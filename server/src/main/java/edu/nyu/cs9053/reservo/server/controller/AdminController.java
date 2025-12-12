package edu.nyu.cs9053.reservo.server.controller;

import edu.nyu.cs9053.reservo.server.model.Resource;
import edu.nyu.cs9053.reservo.server.service.AuthService;
import edu.nyu.cs9053.reservo.server.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthController authController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void checkAdmin(String token) {
        if (token == null || !authController.isValidToken(token)) {
            throw new SecurityException("Invalid or missing token");
        }
        Long userId = authController.getUserIdFromToken(token);
        authService.getUserById(userId).ifPresent(user -> {
            if (!user.getIsAdmin()) {
                throw new SecurityException("Admin access required");
            }
        });
    }

    @PostMapping("/resources")
    public ResponseEntity<?> createResource(
            @RequestBody Resource resource,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            checkAdmin(token);
            Resource created = reservationService.createResource(resource);
            return ResponseEntity.ok(created);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<?> deleteResource(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            checkAdmin(token);
            reservationService.deleteResource(id);
            return ResponseEntity.ok(Map.of("message", "Resource deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/isolation-mode")
    public ResponseEntity<?> setIsolationMode(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            checkAdmin(token);
            String level = request.get("level");
            // Note: Changing isolation level at runtime is complex in Spring
            // This is a simplified demo
            return ResponseEntity.ok(Map.of("message", 
                    "Isolation level change requested: " + level,
                    "note", "Full implementation would require connection pool reconfiguration"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/simulate-contention")
    public ResponseEntity<?> simulateContention(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            checkAdmin(token);
            Long timeSlotId = ((Number) request.get("timeSlotId")).longValue();
            Integer numThreads = ((Number) request.get("numThreads")).intValue();
            Integer qty = ((Number) request.getOrDefault("qty", 1)).intValue();

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < numThreads; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        String requestId = UUID.randomUUID().toString();
                        Long userId = 1L + (threadNum % 3); // Use test users
                        reservationService.placeHold(userId, timeSlotId, qty, requestId);
                        Thread.sleep(100); // Simulate user thinking time
                        // Get hold ID from user's holds
                        List<Map<String, Object>> holds = reservationService.getUserHolds(userId);
                        if (!holds.isEmpty()) {
                            Long holdId = ((Number) holds.get(0).get("id")).longValue();
                            reservationService.confirmHold(userId, holdId);
                        }
                    } catch (Exception e) {
                        // Log contention failures
                    }
                });
            }
            executor.shutdown();

            return ResponseEntity.ok(Map.of("message", 
                    String.format("Started %d concurrent booking attempts", numThreads)));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}

