package gym.client.dto;

import java.util.List;

public record TrainerWorkloadResponse (
        String username,
        String firstName,
        String lastName,
        Boolean active,
        List<YearSummaryResponse> years
) {}

