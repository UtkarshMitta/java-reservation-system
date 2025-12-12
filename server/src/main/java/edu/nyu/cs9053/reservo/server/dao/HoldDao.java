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
public class HoldDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long create(Long userId, Long timeSlotId, Integer qty, 
                      LocalDateTime expiresAt, String requestId) {
        String sql = "INSERT INTO hold (user_id, time_slot_id, qty, expires_at, request_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, timeSlotId);
            ps.setInt(3, qty);
            ps.setObject(4, expiresAt);
            ps.setString(5, requestId);
            return ps;
        }, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        return ((Number) keys.get("ID")).longValue();
    }

    public Optional<Map<String, Object>> findById(Long id) {
        String sql = "SELECT * FROM hold WHERE id = ?";
        List<Map<String, Object>> holds = jdbcTemplate.queryForList(sql, id);
        return holds.isEmpty() ? Optional.empty() : Optional.of(holds.get(0));
    }

    public Optional<Map<String, Object>> findByRequestId(String requestId) {
        String sql = "SELECT * FROM hold WHERE request_id = ?";
        List<Map<String, Object>> holds = jdbcTemplate.queryForList(sql, requestId);
        return holds.isEmpty() ? Optional.empty() : Optional.of(holds.get(0));
    }

    @Transactional
    public boolean delete(Long id) {
        String sql = "DELETE FROM hold WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public List<Map<String, Object>> findExpired(LocalDateTime now) {
        String sql = "SELECT * FROM hold WHERE expires_at < ?";
        return jdbcTemplate.queryForList(sql, now);
    }

    public List<Map<String, Object>> findByUser(Long userId) {
        String sql = "SELECT * FROM hold WHERE user_id = ? ORDER BY expires_at";
        return jdbcTemplate.queryForList(sql, userId);
    }
}

