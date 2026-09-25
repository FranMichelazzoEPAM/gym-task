package gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class TrainingRegistrationRequest {

    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    private String name;

    @NotNull(message = "Training date is required")
    private LocalDate date;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be a positive number")
    private Integer duration;

    public TrainingRegistrationRequest() {
        // Required by Jackson
    }

    public TrainingRegistrationRequest(
            String traineeUsername, String trainerUsername, String name, LocalDate date, Integer duration
    ) {
        this.traineeUsername = traineeUsername;
        this.trainerUsername = trainerUsername;
        this.name = name;
        this.date = date;
        this.duration = duration;
    }

    public String getTraineeUsername() { return traineeUsername; }
    public String getTrainerUsername() { return trainerUsername; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public Integer getDuration() { return duration; }
}
