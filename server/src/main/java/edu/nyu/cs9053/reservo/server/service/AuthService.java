package edu.nyu.cs9053.reservo.server.service;

import edu.nyu.cs9053.reservo.server.dao.UserDao;
import edu.nyu.cs9053.reservo.server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Optional<String> login(String username, String password) {
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            // Simple token generation (in production, use JWT)
            String token = UUID.randomUUID().toString();
            return Optional.of(token);
        }
        return Optional.empty();
    }

    public User register(String username, String password, String email) {
        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate email domain
        if (email == null || !email.toLowerCase().endsWith("@nyu.edu")) {
            throw new IllegalArgumentException("Email must be from @nyu.edu domain");
        }

        // Check if email is already registered
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setIsAdmin(false);

        return userDao.create(user);
    }

    public void updateEmail(Long userId, String newEmail) {
        if (newEmail == null || !newEmail.toLowerCase().endsWith("@nyu.edu")) {
            throw new IllegalArgumentException("Email must be from @nyu.edu domain");
        }

        // Check if email is already used by another user
        Optional<User> existingUser = userDao.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new IllegalArgumentException("Email already registered");
        }

        userDao.updateEmail(userId, newEmail);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 3) {
            throw new IllegalArgumentException("New password must be at least 3 characters");
        }

        userDao.updatePassword(userId, passwordEncoder.encode(newPassword));
    }

    public Optional<User> getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }
}

