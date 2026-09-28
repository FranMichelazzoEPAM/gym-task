package gym.mapper;

import gym.domain.Training;
import gym.dto.response.TrainingSummaryResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TrainingMapper {
    private TrainingMapper() {
        // Utility class, no instances are created
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static TrainingSummaryResponse toSummary(Training training) {
        String trainerName = training.getTrainer().getUser().getFirstName()
                + " " + training.getTrainer().getUser().getLastName();

        return new TrainingSummaryResponse(
                training.getTrainingName(),
                toLocalDate(training.getTrainingDate()),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                trainerName
        );
    }
}
