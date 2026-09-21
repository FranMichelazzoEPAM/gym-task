package gym.service.impl;

import gym.domain.User;
import gym.repository.UserRepository;
import gym.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        LOG.debug("Changing password for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User with username " + username + " not found."));

        if (!user.getPassword().equals(oldPassword)) {
            LOG.error("Password change failed: old password mismatch for {}", username);
            throw new IllegalArgumentException("Old password does not match.");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
        LOG.info("Password changed for user: {}", username);
    }
}