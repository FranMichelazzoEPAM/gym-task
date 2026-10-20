package gym.service.result;

import gym.domain.Trainee;

public record TraineeRegistrationResult(Trainee trainee, String rawPassword) {
}
