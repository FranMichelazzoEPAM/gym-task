package gym.service;

import gym.domain.TrainingType;

import java.util.List;

public interface TrainingTypeService {
    List<TrainingType> getAllTrainingTypes();
    TrainingType getTrainingTypeByName(String name);
}
