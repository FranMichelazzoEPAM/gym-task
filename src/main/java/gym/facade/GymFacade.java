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
    Trainee updateTrainee(String callerUsername, String callerPassword, Trainee trainee);
    void deleteTrainee(String callerUsername, String callerPassword, String username);
    Trainee getTrainee(String callerUsername, String callerPassword, String username);
    List<Trainee> getAllTrainees(String callerUsername, String callerPassword);
    boolean authenticateTrainee(String username, String password);
    void changeTraineePassword(String username, String oldPassword, String newPassword);
    void toggleTraineeActiveStatus(String callerUsername, String callerPassword, String username);
    Trainee updateTraineeTrainersList(String callerUsername, String callerPassword,
                                      String traineeUsername, List<String> trainerUsernames);

    // Trainers
    Trainer createTrainer(String firstName, String lastName, List<TrainingType> specialization);
    Trainer updateTrainer(String callerUsername, String callerPassword, Trainer trainer);
    Trainer getTrainer(String callerUsername, String callerPassword, String username);
    List<Trainer> getAllTrainers(String callerUsername, String callerPassword);
    boolean authenticateTrainer(String username, String password);
    void changeTrainerPassword(String username, String oldPassword, String newPassword);
    void toggleTrainerActiveStatus(String callerUsername, String callerPassword, String username);
    List<Trainer> getTrainersNotAssignedToTrainee(String callerUsername, String callerPassword, String traineeUsername);

    // Trainings
    Training createTraining(String callerUsername, String callerPassword,
                            String traineeUsername, String trainerUsername, String trainingName,
                            TrainingType trainingType, Date trainingDate, int trainingDuration);
    Training getTraining(String callerUsername, String callerPassword, UUID trainingId);
    List<Training> getAllTrainings(String callerUsername, String callerPassword);
    List<Training> getTraineeTrainings(String callerUsername, String callerPassword,
                                       String traineeUsername, Date fromDate, Date toDate,
                                       String trainerName, String trainingTypeName);
    List<Training> getTrainerTrainings(String callerUsername, String callerPassword,
                                       String trainerUsername, Date fromDate, Date toDate,
                                       String traineeName);
}