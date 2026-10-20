package gym.client.dto;

import java.time.LocalDate;

public record TrainerWorkloadRequest (
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        LocalDate trainingDate,
        Double trainingDuration,
        ActionType actionType
) {}
