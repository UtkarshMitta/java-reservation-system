package edu.nyu.cs9053.reservo.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class NotificationDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long create(Long userId, String type, String message) {
        String sql = "INSERT INTO notification (user_id, type, message) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, type);
            ps.setString(3, message);
            return ps;
        }, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        return ((Number) keys.get("ID")).longValue();
    }

    public List<Map<String, Object>> findByUser(Long userId) {
        String sql = "SELECT * FROM notification WHERE user_id = ? " +
                    "ORDER BY created_at DESC";
        return jdbcTemplate.queryForList(sql, userId);
    }

    public void markAsRead(Long id) {
        String sql = "UPDATE notification SET read = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

