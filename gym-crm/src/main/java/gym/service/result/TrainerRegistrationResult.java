package gym.service.result;

import gym.domain.Trainer;

public record TrainerRegistrationResult(Trainer trainer, String rawPassword) {
}
