package edu.nyu.cs9053.reservo.server.dao;

import edu.nyu.cs9053.reservo.server.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TimeSlotDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<TimeSlot> TIME_SLOT_MAPPER = new RowMapper<TimeSlot>() {
        @Override
        public TimeSlot mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeSlot slot = new TimeSlot();
            slot.setId(rs.getLong("id"));
            slot.setResourceId(rs.getLong("resource_id"));
            slot.setStartTs(rs.getTimestamp("start_ts").toLocalDateTime());
            slot.setEndTs(rs.getTimestamp("end_ts").toLocalDateTime());
            slot.setCapacityRemaining(rs.getInt("capacity_remaining"));
            slot.setVersion(rs.getInt("version"));
            if (rs.getTimestamp("created_at") != null) {
                slot.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return slot;
        }
    };

    public List<TimeSlot> findByResourceAndDateRange(Long resourceId, 
                                                      LocalDateTime from, 
                                                      LocalDateTime to) {
        String sql = "SELECT * FROM time_slot WHERE resource_id = ? " +
                    "AND start_ts >= ? AND start_ts < ? ORDER BY start_ts";
        return jdbcTemplate.query(sql, TIME_SLOT_MAPPER, resourceId, from, to);
    }

    public Optional<TimeSlot> findById(Long id) {
        String sql = "SELECT * FROM time_slot WHERE id = ?";
        List<TimeSlot> slots = jdbcTemplate.query(sql, TIME_SLOT_MAPPER, id);
        return slots.isEmpty() ? Optional.empty() : Optional.of(slots.get(0));
    }

    @Transactional
    public Optional<TimeSlot> findByIdForUpdate(Long id) {
        String sql = "SELECT * FROM time_slot WHERE id = ? FOR UPDATE";
        List<TimeSlot> slots = jdbcTemplate.query(sql, TIME_SLOT_MAPPER, id);
        return slots.isEmpty() ? Optional.empty() : Optional.of(slots.get(0));
    }

    @Transactional
    public boolean decrementCapacity(Long id, Integer qty, Integer expectedVersion) {
        String sql = "UPDATE time_slot SET capacity_remaining = capacity_remaining - ?, " +
                    "version = version + 1 WHERE id = ? AND version = ? " +
                    "AND capacity_remaining >= ?";
        int updated = jdbcTemplate.update(sql, qty, id, expectedVersion, qty);
        return updated > 0;
    }

    @Transactional
    public boolean incrementCapacity(Long id, Integer qty) {
        String sql = "UPDATE time_slot SET capacity_remaining = capacity_remaining + ?, " +
                    "version = version + 1 WHERE id = ?";
        int updated = jdbcTemplate.update(sql, qty, id);
        return updated > 0;
    }

    @Transactional
    public boolean incrementCapacity(Long id, Integer qty, Integer expectedVersion) {
        String sql = "UPDATE time_slot SET capacity_remaining = capacity_remaining + ?, " +
                    "version = version + 1 WHERE id = ? AND version = ?";
        int updated = jdbcTemplate.update(sql, qty, id, expectedVersion);
        return updated > 0;
    }

    public void create(TimeSlot slot) {
        String sql = "INSERT INTO time_slot (resource_id, start_ts, end_ts, " +
                    "capacity_remaining, version) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, slot.getResourceId(), slot.getStartTs(), 
                          slot.getEndTs(), slot.getCapacityRemaining(), slot.getVersion());
    }

    public List<TimeSlot> findByResource(Long resourceId) {
        String sql = "SELECT * FROM time_slot WHERE resource_id = ? ORDER BY start_ts";
        return jdbcTemplate.query(sql, TIME_SLOT_MAPPER, resourceId);
    }

    @Transactional
    public boolean delete(Long id) {
        String sql = "DELETE FROM time_slot WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}

