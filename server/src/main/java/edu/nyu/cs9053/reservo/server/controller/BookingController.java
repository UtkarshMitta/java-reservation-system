package edu.nyu.cs9053.reservo.server.controller;

import edu.nyu.cs9053.reservo.server.controller.AuthController;
import edu.nyu.cs9053.reservo.server.dto.HoldRequest;
import edu.nyu.cs9053.reservo.server.dto.HoldResponse;
import edu.nyu.cs9053.reservo.server.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AuthController authController;

    private Long getUserId(String token) {
        if (token == null || !authController.isValidToken(token)) {
            throw new SecurityException("Invalid or missing token");
        }
        return authController.getUserIdFromToken(token);
    }

    @PostMapping("/holds")
    public ResponseEntity<?> placeHold(
            @RequestBody HoldRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            HoldResponse response = reservationService.placeHold(
                    userId, request.getTimeSlotId(), request.getQty(), request.getRequestId());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), 
                    "type", e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/holds/{id}/confirm")
    public ResponseEntity<?> confirmHold(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            Long reservationId = reservationService.confirmHold(userId, id);
            return ResponseEntity.ok(Map.of("reservationId", reservationId, "message", "Reservation confirmed"));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/holds/{id}")
    public ResponseEntity<?> cancelHold(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            reservationService.cancelHold(userId, id);
            return ResponseEntity.ok(Map.of("message", "Hold cancelled successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancelReservation(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            reservationService.cancelReservation(userId, id);
            return ResponseEntity.ok(Map.of("message", "Reservation cancelled"));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/waitlist")
    public ResponseEntity<?> joinWaitlist(
            @RequestBody Map<String, Long> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            Long timeSlotId = request.get("timeSlotId");
            reservationService.joinWaitlist(userId, timeSlotId);
            return ResponseEntity.ok(Map.of("message", "Added to waitlist"));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<?> getMyReservations(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            return ResponseEntity.ok(reservationService.getUserReservations(userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-waitlist")
    public ResponseEntity<?> getMyWaitlist(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            return ResponseEntity.ok(reservationService.getUserWaitlist(userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-holds")
    public ResponseEntity<?> getMyHolds(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            return ResponseEntity.ok(reservationService.getUserHolds(userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserId(token);
            return ResponseEntity.ok(reservationService.getUserNotifications(userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}

