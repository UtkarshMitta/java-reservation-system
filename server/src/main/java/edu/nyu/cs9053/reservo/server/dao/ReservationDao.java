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
public class ReservationDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long create(Long userId, Long timeSlotId, Integer qty, 
                      String status, String requestId) {
        String sql = "INSERT INTO reservation (user_id, time_slot_id, qty, status, request_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, timeSlotId);
            ps.setInt(3, qty);
            ps.setString(4, status);
            ps.setString(5, requestId);
            return ps;
        }, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        return ((Number) keys.get("ID")).longValue();
    }

    public Optional<Map<String, Object>> findById(Long id) {
        String sql = "SELECT * FROM reservation WHERE id = ?";
        List<Map<String, Object>> reservations = jdbcTemplate.queryForList(sql, id);
        return reservations.isEmpty() ? Optional.empty() : Optional.of(reservations.get(0));
    }

    public List<Map<String, Object>> findByUser(Long userId) {
        String sql = "SELECT r.id as id, r.user_id as user_id, r.time_slot_id as time_slot_id, " +
                    "r.qty as qty, r.status as status, r.request_id as request_id, " +
                    "r.created_at as created_at, " +
                    "ts.start_ts as start_ts, ts.end_ts as end_ts, " +
                    "res.name as resource_name " +
                    "FROM reservation r " +
                    "JOIN time_slot ts ON r.time_slot_id = ts.id " +
                    "JOIN resources res ON ts.resource_id = res.id " +
                    "WHERE r.user_id = ? AND r.status = 'CONFIRMED' " +
                    "ORDER BY ts.start_ts";
        return jdbcTemplate.queryForList(sql, userId);
    }

    @Transactional
    public boolean cancel(Long id) {
        String sql = "UPDATE reservation SET status = 'CANCELLED' WHERE id = ? AND status = 'CONFIRMED'";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public Optional<Map<String, Object>> findByRequestId(String requestId) {
        String sql = "SELECT * FROM reservation WHERE request_id = ?";
        List<Map<String, Object>> reservations = jdbcTemplate.queryForList(sql, requestId);
        return reservations.isEmpty() ? Optional.empty() : Optional.of(reservations.get(0));
    }

    public List<Map<String, Object>> findByResource(Long resourceId) {
        String sql = "SELECT r.*, ts.resource_id " +
                    "FROM reservation r " +
                    "JOIN time_slot ts ON r.time_slot_id = ts.id " +
                    "WHERE ts.resource_id = ? AND r.status = 'CONFIRMED'";
        return jdbcTemplate.queryForList(sql, resourceId);
    }
}

