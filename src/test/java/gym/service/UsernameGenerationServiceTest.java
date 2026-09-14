package gym.service;

import gym.repository.UserRepository;
import gym.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class UsernameGenerationServiceTest {

    @Test
    public void testGenerateUsernameNoCollision() {
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.existsByUsername("First.Last")).thenReturn(false);

        UsernameGenerationService svc = new UsernameGenerationService(mockRepo);
        String username = svc.generateUsername("First","Last");
        Assertions.assertEquals("First.Last", username);
    }

    @Test
    public void testGenerateUsernameWithCollisions() {
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.existsByUsername("John.Doe")).thenReturn(true);
        when(mockRepo.existsByUsername("John.Doe1")).thenReturn(false);

        UsernameGenerationService svc = new UsernameGenerationService(mockRepo);
        String username = svc.generateUsername("John","Doe");
        Assertions.assertEquals("John.Doe1", username);
    }
}
