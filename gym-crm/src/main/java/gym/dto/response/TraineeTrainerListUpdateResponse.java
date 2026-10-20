package gym.dto.response;

import java.util.List;

public class TraineeTrainerListUpdateResponse {
    private final List<TrainerSummaryResponse> trainers;

    public TraineeTrainerListUpdateResponse(List<TrainerSummaryResponse> trainers) {
        this.trainers = trainers;
    }

    public List<TrainerSummaryResponse> getTrainers() { return trainers; }
}
