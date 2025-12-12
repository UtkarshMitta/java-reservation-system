package edu.nyu.cs9053.reservo.server.service;

import edu.nyu.cs9053.reservo.server.dao.*;
import edu.nyu.cs9053.reservo.server.dto.AvailabilityResponse;
import edu.nyu.cs9053.reservo.server.dto.HoldResponse;
import edu.nyu.cs9053.reservo.server.model.Resource;
import edu.nyu.cs9053.reservo.server.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private TimeSlotDao timeSlotDao;

    @Autowired
    private HoldDao holdDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private WaitlistDao waitlistDao;

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TimeSlotGeneratorService timeSlotGeneratorService;

    @Value("${reservo.hold.ttl-seconds:60}")
    private int holdTtlSeconds;

    public List<Resource> getAllResources() {
        return resourceDao.findAll();
    }

    public Resource createResource(Resource resource) {
        return resourceDao.create(resource);
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceDao.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        // Get all users who have reservations for this resource's time slots
        // We need to notify them before deletion
        List<Map<String, Object>> affectedReservations = reservationDao.findByResource(resourceId);
        
        // Collect unique user IDs
        java.util.Set<Long> affectedUserIds = new java.util.HashSet<>();
        for (Map<String, Object> res : affectedReservations) {
            Long userId = ((Number) getCaseInsensitive(res, "user_id")).longValue();
            affectedUserIds.add(userId);
        }

        // Delete the resource (CASCADE will delete time_slots, which will CASCADE delete reservations, holds, waitlist)
        boolean deleted = resourceDao.delete(resourceId);
        if (!deleted) {
            throw new IllegalStateException("Failed to delete resource");
        }

        // Log audit event
        auditDao.logEvent("RESOURCE_DELETED", String.format(
                "{\"resourceId\":%d,\"resourceName\":\"%s\",\"affectedUsers\":%d}",
                resourceId, resource.getName(), affectedUserIds.size()));

        // Notify affected users
        for (Long userId : affectedUserIds) {
            notificationDao.create(userId, "RESOURCE_DELETED",
                    String.format("Resource '%s' has been deleted. Your reservation has been cancelled.", resource.getName()));
            
            // Broadcast event for this user
            broadcastEvent("ResourceDeleted", Map.of(
                    "resourceId", resourceId,
                    "resourceName", resource.getName(),
                    "userId", userId
            ));
        }

        // Broadcast general availability change
        broadcastEvent("ResourceDeleted", Map.of(
                "resourceId", resourceId,
                "resourceName", resource.getName()
        ));
    }

    private Object getCaseInsensitive(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value != null) return value;
        value = map.get(key.toUpperCase());
        if (value != null) return value;
        return map.get(key.toLowerCase());
    }

    public AvailabilityResponse getAvailability(Long resourceId, LocalDateTime from, LocalDateTime to) {
        Resource resource = resourceDao.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        // Generate slots if they don't exist yet (extend range to ensure we have slots)
        LocalDateTime generateFrom = from;
        LocalDateTime generateTo = to.isBefore(from.plusDays(7)) ? from.plusDays(7) : to;
        
        List<TimeSlot> existingSlots = timeSlotDao.findByResourceAndDateRange(resourceId, generateFrom, generateTo);
        if (existingSlots.isEmpty()) {
            // Generate slots for this resource directly (avoid circular dependency)
            try {
                generateSlotsForResourceDirectly(resource, generateFrom, generateTo);
            } catch (Exception e) {
                // Log but continue - slots might already exist
                System.err.println("Warning: Could not generate slots: " + e.getMessage());
                e.printStackTrace();
            }
        }

        List<TimeSlot> slots = timeSlotDao.findByResourceAndDateRange(resourceId, from, to);

        List<AvailabilityResponse.SlotAvailability> slotAvailabilities = slots != null ? slots.stream()
                .map(slot -> new AvailabilityResponse.SlotAvailability(
                        slot.getId(),
                        slot.getStartTs(),
                        slot.getEndTs(),
                        slot.getCapacityRemaining(),
                        resource.getCapacity()
                ))
                .collect(Collectors.toList()) : new ArrayList<>();

        return new AvailabilityResponse(resourceId, resource.getName(), slotAvailabilities);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HoldResponse placeHold(Long userId, Long timeSlotId, Integer qty, String requestId) {
        TimeSlot slot = timeSlotDao.findById(timeSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));

        if (slot.getCapacityRemaining() < qty) {
            throw new IllegalStateException("Insufficient capacity");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(holdTtlSeconds);

        // Check if user already has a hold for this slot
        Optional<Map<String, Object>> existingHold = holdDao.findByRequestId(requestId);
        if (existingHold.isPresent()) {
            Map<String, Object> hold = existingHold.get();
            return new HoldResponse(
                    ((Number) hold.get("ID")).longValue(),
                    ((Number) hold.get("TIME_SLOT_ID")).longValue(),
                    ((Number) hold.get("QTY")).intValue(),
                    ((java.sql.Timestamp) hold.get("EXPIRES_AT")).toLocalDateTime(),
                    (String) hold.get("REQUEST_ID")
            );
        }

        Long holdId = holdDao.create(userId, timeSlotId, qty, expiresAt, requestId);

        auditDao.logEvent("HOLD_PLACED", String.format(
                "{\"userId\":%d,\"timeSlotId\":%d,\"qty\":%d,\"requestId\":\"%s\"}",
                userId, timeSlotId, qty, requestId));

        broadcastEvent("HoldPlaced", Map.of(
                "timeSlotId", timeSlotId,
                "userId", userId,
                "qty", qty
        ));

        return new HoldResponse(holdId, timeSlotId, qty, expiresAt, requestId);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long confirmHold(Long userId, Long holdId) {
        Map<String, Object> holdData = holdDao.findById(holdId)
                .orElseThrow(() -> new IllegalArgumentException("Hold not found"));

        Long holdUserId = ((Number) holdData.get("USER_ID")).longValue();
        if (!holdUserId.equals(userId)) {
            throw new SecurityException("Hold belongs to different user");
        }

        LocalDateTime expiresAt = ((java.sql.Timestamp) holdData.get("EXPIRES_AT")).toLocalDateTime();
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Hold has expired");
        }

        Long timeSlotId = ((Number) holdData.get("TIME_SLOT_ID")).longValue();
        Integer qty = ((Number) holdData.get("QTY")).intValue();
        String requestId = (String) holdData.get("REQUEST_ID");

        // Pessimistic lock on time slot
        TimeSlot slot = timeSlotDao.findByIdForUpdate(timeSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));

        if (slot.getCapacityRemaining() < qty) {
            throw new IllegalStateException("Insufficient capacity - conflict detected");
        }

        // Create reservation
        Long reservationId = reservationDao.create(userId, timeSlotId, qty, "CONFIRMED", requestId);

        // Decrement capacity
        if (!timeSlotDao.decrementCapacity(timeSlotId, qty, slot.getVersion())) {
            throw new IllegalStateException("Version conflict - capacity changed");
        }

        // Delete hold
        holdDao.delete(holdId);

        auditDao.logEvent("RESERVATION_CONFIRMED", String.format(
                "{\"userId\":%d,\"timeSlotId\":%d,\"qty\":%d,\"reservationId\":%d,\"requestId\":\"%s\"}",
                userId, timeSlotId, qty, reservationId, requestId));

        broadcastEvent("ReservationCreated", Map.of(
                "reservationId", reservationId,
                "timeSlotId", timeSlotId,
                "userId", userId,
                "qty", qty
        ));

        return reservationId;
    }

    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Map<String, Object> reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        Long resUserId = ((Number) reservation.get("user_id")).longValue();
        if (!resUserId.equals(userId)) {
            throw new SecurityException("Reservation belongs to different user");
        }

        Long timeSlotId = ((Number) reservation.get("time_slot_id")).longValue();
        Integer qty = ((Number) reservation.get("qty")).intValue();

        if (!reservationDao.cancel(reservationId)) {
            throw new IllegalStateException("Reservation already cancelled");
        }

        timeSlotDao.incrementCapacity(timeSlotId, qty);

        auditDao.logEvent("RESERVATION_CANCELLED", String.format(
                "{\"userId\":%d,\"timeSlotId\":%d,\"qty\":%d,\"reservationId\":%d}",
                userId, timeSlotId, qty, reservationId));

        broadcastEvent("ReservationCancelled", Map.of(
                "reservationId", reservationId,
                "timeSlotId", timeSlotId,
                "userId", userId,
                "qty", qty
        ));

        // Trigger waitlist promotion check
        checkAndPromoteWaitlist(timeSlotId);
    }

    public void joinWaitlist(Long userId, Long timeSlotId) {
        if (waitlistDao.exists(userId, timeSlotId)) {
            throw new IllegalStateException("Already on waitlist for this slot");
        }

        waitlistDao.create(userId, timeSlotId);

        auditDao.logEvent("WAITLIST_JOINED", String.format(
                "{\"userId\":%d,\"timeSlotId\":%d}",
                userId, timeSlotId));
    }

    @Transactional
    public void checkAndPromoteWaitlist(Long timeSlotId) {
        TimeSlot slot = timeSlotDao.findById(timeSlotId).orElse(null);
        if (slot == null || slot.getCapacityRemaining() == 0) {
            return;
        }

        Optional<Map<String, Object>> waitlistEntry = waitlistDao.findFirstByTimeSlot(timeSlotId);
        if (waitlistEntry.isEmpty()) {
            return;
        }

        Map<String, Object> entry = waitlistEntry.get();
        Long userId = ((Number) entry.get("user_id")).longValue();
        Long waitlistId = ((Number) entry.get("id")).longValue();

        // Promote to reservation
        String requestId = UUID.randomUUID().toString();
        reservationDao.create(userId, timeSlotId, 1, "CONFIRMED", requestId);
        timeSlotDao.decrementCapacity(timeSlotId, 1, slot.getVersion());
        waitlistDao.delete(waitlistId);

        notificationDao.create(userId, "PROMOTED", 
                String.format("You have been promoted from waitlist for slot %d", timeSlotId));

        auditDao.logEvent("WAITLIST_PROMOTED", String.format(
                "{\"userId\":%d,\"timeSlotId\":%d,\"reservationId\":%d}",
                userId, timeSlotId, reservationDao.findByRequestId(requestId)
                        .map(r -> ((Number) r.get("id")).longValue())
                        .orElse(-1L)));

        broadcastEvent("Promoted", Map.of(
                "userId", userId,
                "timeSlotId", timeSlotId
        ));

        broadcastEvent("AvailabilityChanged", Map.of(
                "timeSlotId", timeSlotId
        ));
    }

    public void expireHolds() {
        List<Map<String, Object>> expiredHolds = holdDao.findExpired(LocalDateTime.now());
        for (Map<String, Object> hold : expiredHolds) {
            Long holdId = ((Number) hold.get("id")).longValue();
            Long timeSlotId = ((Number) hold.get("time_slot_id")).longValue();
            Long userId = ((Number) hold.get("user_id")).longValue();

            holdDao.delete(holdId);

            auditDao.logEvent("HOLD_EXPIRED", String.format(
                    "{\"userId\":%d,\"timeSlotId\":%d,\"holdId\":%d}",
                    userId, timeSlotId, holdId));

            broadcastEvent("HoldExpired", Map.of(
                    "holdId", holdId,
                    "timeSlotId", timeSlotId,
                    "userId", userId
            ));

            broadcastEvent("AvailabilityChanged", Map.of(
                    "timeSlotId", timeSlotId
            ));

            checkAndPromoteWaitlist(timeSlotId);
        }
    }

    private void broadcastEvent(String eventType, Map<String, Object> data) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", eventType);
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("data", data);
        messagingTemplate.convertAndSend("/topic/events", event);
    }

    public List<Map<String, Object>> getUserReservations(Long userId) {
        return reservationDao.findByUser(userId);
    }

    public List<Map<String, Object>> getUserWaitlist(Long userId) {
        return waitlistDao.findByUser(userId);
    }

    public List<Map<String, Object>> getUserNotifications(Long userId) {
        return notificationDao.findByUser(userId);
    }

    public List<Map<String, Object>> getUserHolds(Long userId) {
        return holdDao.findByUser(userId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelHold(Long userId, Long holdId) {
        Optional<Map<String, Object>> holdOpt = holdDao.findById(holdId);
        if (holdOpt.isEmpty()) {
            throw new IllegalArgumentException("Hold not found");
        }

        Map<String, Object> hold = holdOpt.get();
        Long holdUserId = ((Number) getCaseInsensitive(hold, "user_id")).longValue();
        
        if (!holdUserId.equals(userId)) {
            throw new SecurityException("Hold belongs to different user");
        }

        Long timeSlotId = ((Number) getCaseInsensitive(hold, "time_slot_id")).longValue();
        Integer qty = ((Number) getCaseInsensitive(hold, "qty")).intValue();

        // Get time slot to restore capacity
        Optional<TimeSlot> slotOpt = timeSlotDao.findById(timeSlotId);
        if (slotOpt.isPresent()) {
            TimeSlot slot = slotOpt.get();
            // Restore capacity (no version check needed for incrementing)
            timeSlotDao.incrementCapacity(timeSlotId, qty);
        }

        // Delete the hold
        holdDao.delete(holdId);

        // Log audit event
        auditDao.logEvent("HOLD_CANCELLED", String.format(
                "{\"userId\":%d,\"timeSlotId\":%d,\"holdId\":%d,\"qty\":%d}",
                userId, timeSlotId, holdId, qty));

        // Broadcast events
        broadcastEvent("HoldCancelled", Map.of(
                "holdId", holdId,
                "timeSlotId", timeSlotId,
                "userId", userId
        ));

        broadcastEvent("AvailabilityChanged", Map.of(
                "timeSlotId", timeSlotId
        ));

        // Check if we can promote waitlist
        if (slotOpt.isPresent()) {
            checkAndPromoteWaitlist(timeSlotId);
        }
    }

    // Helper method to generate slots directly (avoiding circular dependency)
    private void generateSlotsForResourceDirectly(Resource resource, LocalDateTime from, LocalDateTime to) {
        int durationMinutes = resource.getSlotDurationMinutes();
        LocalDateTime current = from;

        while (current.isBefore(to)) {
            LocalDateTime slotStart = current;
            LocalDateTime slotEnd = current.plusMinutes(durationMinutes);

            // Check if slot already exists
            List<TimeSlot> existing = timeSlotDao.findByResourceAndDateRange(
                    resource.getId(), slotStart.minusMinutes(1), slotEnd.plusMinutes(1));
            
            boolean slotExists = existing.stream().anyMatch(s -> 
                s.getStartTs().equals(slotStart) && s.getEndTs().equals(slotEnd));

            if (!slotExists) {
                TimeSlot slot = new TimeSlot(
                        resource.getId(),
                        slotStart,
                        slotEnd,
                        resource.getCapacity()
                );
                timeSlotDao.create(slot);
            }

            current = current.plusMinutes(durationMinutes);
        }
    }
}

