package edu.nyu.cs9053.reservo.server.dao;

import edu.nyu.cs9053.reservo.server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            // Handle username_hash column (may be null for old records)
            try {
                user.setUsernameHash(rs.getString("username_hash"));
            } catch (SQLException e) {
                // Column doesn't exist or is null - will be set during migration
                user.setUsernameHash(null);
            }
            user.setPasswordHash(rs.getString("password_hash"));
            user.setEmail(rs.getString("email"));
            user.setIsAdmin(rs.getBoolean("is_admin"));
            if (rs.getTimestamp("created_at") != null) {
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return user;
        }
    };

    public Optional<User> findByUsernameHash(String usernameHash) {
        String sql = "SELECT * FROM users WHERE username_hash = ?";
        List<User> users = jdbcTemplate.query(sql, USER_MAPPER, usernameHash);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // Keep for backward compatibility during migration
    @Deprecated
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, USER_MAPPER, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, USER_MAPPER, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User create(User user) {
        String sql = "INSERT INTO users (username, username_hash, password_hash, email, is_admin) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getUsername(), user.getUsernameHash(), user.getPasswordHash(), 
                          user.getEmail(), user.getIsAdmin());
        return findByUsernameHash(user.getUsernameHash()).orElseThrow();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, USER_MAPPER, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public void updateEmail(Long userId, String email) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        jdbcTemplate.update(sql, email, userId);
    }

    public void updatePassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        jdbcTemplate.update(sql, passwordHash, userId);
    }
}

