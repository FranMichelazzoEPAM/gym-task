package gym.dao;

import gym.domain.Trainee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TraineeDao {
    Trainee save(Trainee trainee);
    Optional<Trainee> findById(UUID userId);
    List<Trainee> findAll();
    void delete(UUID userId);
}
