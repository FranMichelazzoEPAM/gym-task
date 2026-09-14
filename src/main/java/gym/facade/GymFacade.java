package gym.facade;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface GymFacade {

    // Trainees
    Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTrainee(Trainee trainee);
    void deleteTrainee(String username);
    Trainee getTrainee(String username);
    List<Trainee> getAllTrainees();
    boolean authenticateTrainee(String username, String password);
    void changeTraineePassword(String username, String oldPassword, String newPassword);
    void toggleTraineeActiveStatus(String username);
    Trainee updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames);

    // Trainers
    Trainer createTrainer(String firstName, String lastName, List<TrainingType> specialization);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainer(String username);
    List<Trainer> getAllTrainers();
    boolean authenticateTrainer(String username, String password);
    void changeTrainerPassword(String username, String oldPassword, String newPassword);
    void toggleTrainerActiveStatus(String username);
    List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername);

    // Trainings
    Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                            TrainingType trainingType, Date trainingDate, int trainingDuration);
    Training getTraining(UUID trainingId);
    List<Training> getAllTrainings();
    List<Training> getTraineeTrainings(String traineeUsername, Date fromDate, Date toDate,
                                       String trainerName, String trainingTypeName);
    List<Training> getTrainerTrainings(String trainerUsername, Date fromDate, Date toDate,
                                       String traineeName);
}