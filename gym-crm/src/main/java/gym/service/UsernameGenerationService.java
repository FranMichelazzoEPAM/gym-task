package gym.service;

import gym.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsernameGenerationService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UsernameGenerationService.class);

    private final UserRepository userRepository;

    @Autowired
    public UsernameGenerationService(UserRepository userRepository) {
        this.userRepository = userRepository;
        LOG.info("UserRepository injected into UsernameGenerationService");
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;
        LOG.debug("Generating username for {} {}. base={}", firstName, lastName, baseUsername);

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            LOG.debug("Username collision, trying {}", username);
            suffix++;
        }
        LOG.info("Generated username={}", username);
        return username;
    }
}