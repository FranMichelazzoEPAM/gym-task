package gym.mapper;

import gym.domain.Trainer;
import gym.dto.response.CredentialsResponse;
import gym.dto.response.TrainerSummaryResponse;

public class TrainerMapper {
    private TrainerMapper() {}

    public static CredentialsResponse toCredentialsResponse(Trainer trainer) {
        return new CredentialsResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword()
        );
    }

    public static TrainerSummaryResponse toSummary(Trainer trainer) {
        return new TrainerSummaryResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }
}
