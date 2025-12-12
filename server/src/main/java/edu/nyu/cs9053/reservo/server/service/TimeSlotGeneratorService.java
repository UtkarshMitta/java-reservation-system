package edu.nyu.cs9053.reservo.server.service;

import edu.nyu.cs9053.reservo.server.dao.ResourceDao;
import edu.nyu.cs9053.reservo.server.dao.TimeSlotDao;
import edu.nyu.cs9053.reservo.server.model.Resource;
import edu.nyu.cs9053.reservo.server.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TimeSlotGeneratorService implements CommandLineRunner {

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private TimeSlotDao timeSlotDao;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void generateTimeSlots() {
        List<Resource> resources = resourceDao.findAll();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime horizon = now.plusDays(30);

        for (Resource resource : resources) {
            generateSlotsForResource(resource, now, horizon);
        }
    }

    public void generateSlotsForResource(Resource resource, LocalDateTime from, LocalDateTime to) {
        int durationMinutes = resource.getSlotDurationMinutes();
        LocalDateTime current = from;

        while (current.isBefore(to)) {
            LocalDateTime slotStart = current;
            LocalDateTime slotEnd = current.plusMinutes(durationMinutes);

            // Check if slot already exists (use a small range to check for exact match)
            List<TimeSlot> existing = timeSlotDao.findByResourceAndDateRange(
                    resource.getId(), slotStart.minusMinutes(1), slotEnd.plusMinutes(1));
            
            // Check if any existing slot overlaps with this one
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

    @Override
    public void run(String... args) throws Exception {
        // Generate initial time slots on startup
        // Use @PostConstruct or delay to avoid circular dependencies
        try {
            generateTimeSlots();
        } catch (Exception e) {
            System.err.println("Warning: Could not generate initial time slots: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

