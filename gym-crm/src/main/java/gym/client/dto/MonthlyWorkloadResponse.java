package gym.client.dto;

public record MonthlyWorkloadResponse(
        String username,
        int year,
        int month,
        double totalDuration
) {}