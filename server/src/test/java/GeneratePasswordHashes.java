package edu.nyu.cs9053.reservo.server.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHashes {
    private static final String PEPPER = "NYU_Reservo_2025_Secure_Pepper_Key_Change_In_Production";
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Hash passwords with pepper
        String adminPass = encoder.encode("admin123" + PEPPER);
        String userPass = encoder.encode("user123" + PEPPER);
        
        System.out.println("admin123 (with pepper): " + adminPass);
        System.out.println("user123 (with pepper): " + userPass);
    }
}

