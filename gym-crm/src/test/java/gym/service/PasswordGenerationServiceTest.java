package gym.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordGenerationServiceTest {

    @Test
    public void testGenerateRandomPasswordLengthAndCharset() {
        PasswordGenerationService svc = new PasswordGenerationService();
        String p = svc.generateRandomPassword();
        Assertions.assertNotNull(p);
        Assertions.assertEquals(10, p.length());
        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (char c : p.toCharArray()) {
            Assertions.assertTrue(allowed.indexOf(c) >= 0, "Character not allowed: " + c);
        }
    }
}
