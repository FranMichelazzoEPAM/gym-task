package gym.facade;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface GymFacade {

    Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTrainee(Trainee trainee);
    void deleteTrainee(UUID userId);
    Trainee getTrainee(UUID userId);
    List<Trainee> getAllTrainees();

    Trainer createTrainer(String firstName, String lastName, String specialization);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainer(UUID userId);
    List<Trainer> getAllTrainers();

    Training createTraining(UUID traineeId, UUID trainerId, String trainingName, gym.domain.TrainingType trainingType, Date trainingDate, Duration trainingDuration);
    Training getTraining(UUID trainingId);
    List<Training> getAllTrainings();
}