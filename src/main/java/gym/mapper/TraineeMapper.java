package gym.mapper;

import gym.domain.Trainee;
import gym.dto.response.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class TraineeMapper {
    private TraineeMapper() {
        // Utility class, no instances
    }
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static CredentialsResponse toCredentialsResponse(Trainee trainee) {
        return new CredentialsResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword()
        );
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static TraineeProfileResponse toProfileResponse(Trainee trainee) {
        List<TrainerSummaryResponse> trainerSummaries = trainee.getTrainers().stream()
                .map(TrainerMapper::toSummary)
                .toList();

        return new TraineeProfileResponse(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                toLocalDate(trainee.getDateOfBirth()),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainerSummaries
        );
    }

    public static TraineeUpdateResponse toUpdateResponse(Trainee trainee) {
        List<TrainerSummaryResponse> trainerSummaries = trainee.getTrainers().stream()
                .map(TrainerMapper::toSummary)
                .toList();

        return new TraineeUpdateResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                toLocalDate(trainee.getDateOfBirth()),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainerSummaries
        );
    }

    public static TraineeSummaryResponse toSummary(Trainee trainee) {
        return new TraineeSummaryResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName());
    }
}
