package gym.mapper;

import gym.domain.Training;
import gym.dto.response.TraineeTrainingSummaryResponse;
import gym.dto.response.TrainerTrainingSummaryResponse;

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

    public static TraineeTrainingSummaryResponse toSummary(Training training) {
        String trainerName = training.getTrainer().getUser().getFirstName()
                + " " + training.getTrainer().getUser().getLastName();

        return new TraineeTrainingSummaryResponse(
                training.getTrainingName(),
                toLocalDate(training.getTrainingDate()),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                trainerName
        );
    }

    public static TrainerTrainingSummaryResponse toTrainerSummary(Training training) {
        String traineeName = training.getTrainee().getUser().getFirstName()
                + " " + training.getTrainee().getUser().getLastName();

        return new TrainerTrainingSummaryResponse(
                training.getTrainingName(),
                toLocalDate(training.getTrainingDate()),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                traineeName
        );
    }
}
