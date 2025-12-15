import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashWithPepperGenerator {
    private static final String PEPPER = "NYU_Reservo_2025_Secure_Pepper_Key_Change_In_Production";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public static String hashPassword(String password) {
        String passwordWithPepper = password + PEPPER;
        return encoder.encode(passwordWithPepper);
    }
    
    public static void main(String[] args) {
        System.out.println("admin123: " + hashPassword("admin123"));
        System.out.println("user123: " + hashPassword("user123"));
    }
}

