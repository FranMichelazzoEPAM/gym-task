package gym.facade;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.service.result.TraineeRegistrationResult;
import gym.service.result.TrainerRegistrationResult;

import java.util.Date;
import java.util.List;

public interface GymFacade {

    // Users
    void changePassword(String username, String oldPassword, String newPassword);

    // Trainees
    TraineeRegistrationResult createTrainee(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTrainee(Trainee trainee);
    void deleteTrainee(String username);
    Trainee getTrainee(String username);
    void updateTraineeActiveStatus(String username, boolean active);
    Trainee updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames);

    // Trainers
    TrainerRegistrationResult createTrainer(String firstName, String lastName, TrainingType specialization);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainer(String username);
    void updateTrainerActiveStatus(String username, boolean active);
    List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername);

    // Trainings
    Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                            TrainingType trainingType, Date trainingDate, int trainingDuration);
    List<Training> getTraineeTrainings(String traineeUsername, Date fromDate, Date toDate,
                                       String trainerName, String trainingTypeName);
    List<Training> getTrainerTrainings(String trainerUsername, Date fromDate, Date toDate,
                                       String traineeName);

    // TrainingTypes
    List<TrainingType> getAllTrainingTypes();
    TrainingType getTrainingTypeByName(String name);
}