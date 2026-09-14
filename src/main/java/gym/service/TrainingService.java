package gym.service;

import gym.domain.Training;
import gym.domain.TrainingType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TrainingService {
    Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                            TrainingType trainingType, Date trainingDate, int trainingDuration);
    Training getTraining(UUID trainingId);
    List<Training> getAllTrainings();

    List<Training> getTraineeTrainings(String traineeUsername, Date fromDate, Date toDate,
                                       String trainerName, String trainingTypeName);
    List<Training> getTrainerTrainings(String trainerUsername, Date fromDate, Date toDate,
                                       String traineeName);
}