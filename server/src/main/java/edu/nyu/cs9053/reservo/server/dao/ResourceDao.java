package edu.nyu.cs9053.reservo.server.dao;

import edu.nyu.cs9053.reservo.server.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ResourceDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Resource> RESOURCE_MAPPER = new RowMapper<Resource>() {
        @Override
        public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
            Resource resource = new Resource();
            resource.setId(rs.getLong("id"));
            resource.setName(rs.getString("name"));
            resource.setCapacity(rs.getInt("capacity"));
            resource.setSlotDurationMinutes(rs.getInt("slot_duration_minutes"));
            resource.setBookingHorizonDays(rs.getInt("booking_horizon_days"));
            resource.setMaxHoursPerDay(rs.getObject("max_hours_per_day") != null ? 
                                      rs.getInt("max_hours_per_day") : null);
            resource.setRulesJson(rs.getString("rules_json"));
            if (rs.getTimestamp("created_at") != null) {
                resource.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return resource;
        }
    };

    public List<Resource> findAll() {
        String sql = "SELECT * FROM resources ORDER BY name";
        return jdbcTemplate.query(sql, RESOURCE_MAPPER);
    }

    public Optional<Resource> findById(Long id) {
        String sql = "SELECT * FROM resources WHERE id = ?";
        List<Resource> resources = jdbcTemplate.query(sql, RESOURCE_MAPPER, id);
        return resources.isEmpty() ? Optional.empty() : Optional.of(resources.get(0));
    }

    public Resource create(Resource resource) {
        String sql = "INSERT INTO resources (name, capacity, slot_duration_minutes, " +
                    "booking_horizon_days, max_hours_per_day, rules_json) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, resource.getName());
            ps.setInt(2, resource.getCapacity());
            ps.setInt(3, resource.getSlotDurationMinutes());
            ps.setInt(4, resource.getBookingHorizonDays());
            ps.setInt(5, resource.getMaxHoursPerDay());
            ps.setString(6, resource.getRulesJson());
            return ps;
        }, keyHolder);
        java.util.Map<String, Object> keys = keyHolder.getKeys();
        Long id = ((Number) keys.get("ID")).longValue();
        return findById(id).orElseThrow();
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean delete(Long id) {
        String sql = "DELETE FROM resources WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}

