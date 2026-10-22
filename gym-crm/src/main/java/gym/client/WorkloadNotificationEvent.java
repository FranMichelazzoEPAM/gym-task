package gym.client;

import gym.client.dto.TrainerWorkloadRequest;

public record WorkloadNotificationEvent(TrainerWorkloadRequest request) {}
