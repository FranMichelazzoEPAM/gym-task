package gym.dao;

import gym.domain.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerDao {
    Trainer save(Trainer trainer);
    Optional<Trainer> findById(UUID userId);
    List<Trainer> findAll();
}
