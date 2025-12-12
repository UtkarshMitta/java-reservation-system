package edu.nyu.cs9053.reservo.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class WaitlistDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long create(Long userId, Long timeSlotId) {
        String sql = "INSERT INTO waitlist (user_id, time_slot_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, timeSlotId);
            return ps;
        }, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        return ((Number) keys.get("ID")).longValue();
    }

    public Optional<Map<String, Object>> findFirstByTimeSlot(Long timeSlotId) {
        String sql = "SELECT * FROM waitlist WHERE time_slot_id = ? " +
                    "ORDER BY queued_at LIMIT 1";
        List<Map<String, Object>> entries = jdbcTemplate.queryForList(sql, timeSlotId);
        return entries.isEmpty() ? Optional.empty() : Optional.of(entries.get(0));
    }

    @Transactional
    public boolean delete(Long id) {
        String sql = "DELETE FROM waitlist WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public List<Map<String, Object>> findByUser(Long userId) {
        String sql = "SELECT w.*, ts.start_ts, ts.end_ts, res.name as resource_name, " +
                    "(SELECT COUNT(*) FROM waitlist w2 WHERE w2.time_slot_id = w.time_slot_id " +
                    "AND w2.queued_at < w.queued_at) as position " +
                    "FROM waitlist w " +
                    "JOIN time_slot ts ON w.time_slot_id = ts.id " +
                    "JOIN resources res ON ts.resource_id = res.id " +
                    "WHERE w.user_id = ? ORDER BY w.queued_at";
        return jdbcTemplate.queryForList(sql, userId);
    }

    public boolean exists(Long userId, Long timeSlotId) {
        String sql = "SELECT COUNT(*) FROM waitlist WHERE user_id = ? AND time_slot_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, timeSlotId);
        return count != null && count > 0;
    }
}

