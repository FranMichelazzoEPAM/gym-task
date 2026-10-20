package gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TraineeTrainerListUpdateRequest {

    @NotNull(message = "Trainer's usernames are required")
    public List<@NotBlank(message = "Trainer username cannot be blank") String> trainersUsernames;

    public TraineeTrainerListUpdateRequest() {
        // Required by Jackson
    }

    public TraineeTrainerListUpdateRequest(List<String> trainersUsernames) {
        this.trainersUsernames = trainersUsernames;
    }

    public List<String> getTrainersUsernames() {
        return trainersUsernames;
    }
    public void setTrainersUsernames(List<String> trainersUsernames) {
        this.trainersUsernames = trainersUsernames;
    }
}
