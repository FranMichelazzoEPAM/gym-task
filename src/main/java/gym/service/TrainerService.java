package gym.service;

import gym.domain.Trainer;

import java.util.List;
import java.util.UUID;

public interface TrainerService {
    Trainer createTrainer(String firstName, String lastName, String specialization);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainer(UUID userId);
    List<Trainer> getAllTrainers();
}
