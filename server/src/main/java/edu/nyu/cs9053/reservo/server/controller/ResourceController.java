package edu.nyu.cs9053.reservo.server.controller;

import edu.nyu.cs9053.reservo.server.dto.AvailabilityResponse;
import edu.nyu.cs9053.reservo.server.model.Resource;
import edu.nyu.cs9053.reservo.server.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(reservationService.getAllResources());
    }

    @GetMapping("/{resourceId}/availability")
    public ResponseEntity<?> getAvailability(
            @PathVariable Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            return ResponseEntity.ok(reservationService.getAvailability(resourceId, from, to));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), 
                    "type", e.getClass().getSimpleName()));
        }
    }
}

