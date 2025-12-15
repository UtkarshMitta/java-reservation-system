import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class UsernameHashGenerator {
    private static final String PEPPER = "NYU_Reservo_2025_Secure_Pepper_Key_Change_In_Production";
    
    public static String hashUsername(String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = md.digest((username + "NYU_SALT").getBytes(StandardCharsets.UTF_8));
            
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(PEPPER.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            
            String input = username + Base64.getEncoder().encodeToString(saltBytes);
            byte[] hashBytes = hmac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash username", e);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("admin: " + hashUsername("admin"));
        System.out.println("user1: " + hashUsername("user1"));
        System.out.println("user2: " + hashUsername("user2"));
        System.out.println("user3: " + hashUsername("user3"));
    }
}

