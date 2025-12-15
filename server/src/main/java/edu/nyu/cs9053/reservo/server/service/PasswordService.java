package edu.nyu.cs9053.reservo.server.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service for hashing passwords and usernames with salt+pepper
 */
@Service
public class PasswordService {

    // Application-wide pepper (in production, load from secure config/vault)
    private static final String PEPPER = "NYU_Reservo_2025_Secure_Pepper_Key_Change_In_Production";
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Hash password with salt+pepper
     * Process: password + pepper -> BCrypt (which adds its own salt)
     */
    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        // Add pepper before BCrypt encoding
        String passwordWithPepper = password + PEPPER;
        return passwordEncoder.encode(passwordWithPepper);
    }

    /**
     * Verify password against hash
     */
    public boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        // Add pepper before verification
        String passwordWithPepper = password + PEPPER;
        return passwordEncoder.matches(passwordWithPepper, hash);
    }

    /**
     * Hash username with salt+pepper using SHA-256 + HMAC
     * Process: HMAC-SHA256(username + salt, pepper)
     */
    public String hashUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        try {
            // Generate a deterministic salt from username (for consistency)
            // In production, you might want to store salt separately
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = md.digest((username + "NYU_SALT").getBytes(StandardCharsets.UTF_8));
            
            // Use HMAC-SHA256 with pepper as key
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(PEPPER.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            
            // Hash: username + salt
            String input = username + Base64.getEncoder().encodeToString(saltBytes);
            byte[] hashBytes = hmac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash username", e);
        }
    }

    /**
     * Verify username hash (for consistency checking)
     */
    public boolean verifyUsernameHash(String username, String hash) {
        if (username == null || hash == null) {
            return false;
        }
        String computedHash = hashUsername(username);
        return computedHash.equals(hash);
    }
}

