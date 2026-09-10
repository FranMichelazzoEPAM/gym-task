package gym.service;

import gym.domain.Training;
import gym.domain.TrainingType;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TrainingService {
    Training createTraining(UUID traineeId, UUID trainerId, String trainingName, TrainingType trainingType, Date trainingDate, Duration trainingDuration);
    Training getTraining(UUID trainingId);
    List<Training> getAllTrainings();
}
