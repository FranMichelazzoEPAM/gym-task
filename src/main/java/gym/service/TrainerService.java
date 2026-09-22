package gym.service;

import gym.domain.Trainer;
import gym.domain.TrainingType;

import java.util.List;

public interface TrainerService {
    Trainer createTrainer(String firstName, String lastName, List<TrainingType> specialization);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainerByUsername(String username);
    List<Trainer> getAllTrainers();

    boolean authenticate(String username, String password);
    void updateActiveStatus(String username, boolean active);
    List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername);
}