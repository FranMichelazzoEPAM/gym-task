package gym.mapper;

import gym.domain.Trainer;
import gym.dto.response.CredentialsResponse;

public class TrainerMapper {
    private TrainerMapper() {}

    public static CredentialsResponse toCredentialsResponse(Trainer trainer) {
        return new CredentialsResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword()
        );
    }
}
