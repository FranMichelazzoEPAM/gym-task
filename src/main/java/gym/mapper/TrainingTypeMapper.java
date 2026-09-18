package gym.mapper;

import gym.domain.TrainingType;
import gym.dto.response.TrainingTypeResponse;

public class TrainingTypeMapper {
    private TrainingTypeMapper() {}

    public static TrainingTypeResponse toResponse(TrainingType trainingType) {
        return new TrainingTypeResponse(trainingType.getId(), trainingType.getTrainingTypeName());
    }
}
