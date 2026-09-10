package gym.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGenerationService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordGenerationService.class);
    // Define the characters allowed in the random string
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        String pwd = sb.toString();
        LOG.debug("Generated random password of length {}", pwd.length());
        return pwd;
    }
}

