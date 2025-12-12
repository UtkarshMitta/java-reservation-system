package edu.nyu.cs9053.reservo.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AuditDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void logEvent(String type, String payloadJson) {
        String sql = "INSERT INTO audit_event (type, payload_json) VALUES (?, ?)";
        jdbcTemplate.update(sql, type, payloadJson);
    }

    public List<Map<String, Object>> getEventsForTimeSlot(Long timeSlotId, int limit) {
        String sql = "SELECT * FROM audit_event " +
                    "WHERE payload_json LIKE ? " +
                    "ORDER BY ts DESC LIMIT ?";
        String pattern = "%\"timeSlotId\":" + timeSlotId + "%";
        return jdbcTemplate.queryForList(sql, pattern, limit);
    }
}

