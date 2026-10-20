package gym.mapper;

import gym.client.dto.ActionType;
import gym.client.dto.TrainerWorkloadRequest;
import gym.domain.Training;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class TrainerWorkloadRequestMapper {

    private TrainerWorkloadRequestMapper() {}

    public static TrainerWorkloadRequest toRequest(Training training, ActionType actionType) {
        var trainer = training.getTrainer();
        var user = trainer.getUser();

        LocalDate trainingDate = Instant.ofEpochMilli(training.getTrainingDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        double durationInHours = training.getTrainingDuration() / 60.0;

        return new TrainerWorkloadRequest(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                trainingDate,
                durationInHours,
                actionType
        );
    }
}