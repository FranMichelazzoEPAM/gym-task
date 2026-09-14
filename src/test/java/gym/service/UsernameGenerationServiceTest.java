package gym.service;

import gym.dao.TraineeRepository;
import gym.dao.TrainerDao;
import gym.domain.Trainee;
import gym.domain.Trainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class UsernameGenerationServiceTest {

    @Test
    public void testGenerateUsernameNoCollision() {
        UsernameGenerationService svc = new UsernameGenerationService();
        svc.setTraineeDao(new TraineeRepository() {
            @Override public Trainee save(Trainee trainee) { return null; }
            @Override public Optional<Trainee> findById(UUID userId) { return Optional.empty(); }
            @Override public List<Trainee> findAll() { return Collections.emptyList(); }
            @Override public void delete(UUID userId) {}
        });
        svc.setTrainerDao(new TrainerDao() {
            @Override public Trainer save(Trainer trainer) { return null; }
            @Override public Optional<Trainer> findById(UUID userId) { return Optional.empty(); }
            @Override public List<Trainer> findAll() { return Collections.emptyList(); }
        });

        String username = svc.generateUsername("First","Last");
        Assertions.assertEquals("First.Last", username);
    }

    @Test
    public void testGenerateUsernameWithCollisions() {
        UsernameGenerationService svc = new UsernameGenerationService();
        Trainee existing = new Trainee("A","B","John.Doe", "pw", true, new Date(), "addr", UUID.randomUUID());
        List<Trainee> trainees = Arrays.asList(existing);
        svc.setTraineeDao(new TraineeRepository() {
            @Override public Trainee save(Trainee trainee) { return null; }
            @Override public Optional<Trainee> findById(UUID userId) { return Optional.empty(); }
            @Override public List<Trainee> findAll() { return trainees; }
            @Override public void delete(UUID userId) {}
        });
        svc.setTrainerDao(new TrainerDao() {
            @Override public Trainer save(Trainer trainer) { return null; }
            @Override public Optional<Trainer> findById(UUID userId) { return Optional.empty(); }
            @Override public List<Trainer> findAll() { return Collections.emptyList(); }
        });

        String username = svc.generateUsername("John","Doe");
        Assertions.assertEquals("John.Doe1", username);
    }
}
