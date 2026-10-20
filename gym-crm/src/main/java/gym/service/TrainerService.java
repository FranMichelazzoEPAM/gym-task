package gym.service;

import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.service.result.TrainerRegistrationResult;

import java.util.List;

public interface TrainerService {
    TrainerRegistrationResult createTrainer(String firstName, String lastName, TrainingType specialization);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainerByUsername(String username);
    List<Trainer> getAllTrainers();

    boolean authenticate(String username, String password);
    void updateActiveStatus(String username, boolean active);
    List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername);
}