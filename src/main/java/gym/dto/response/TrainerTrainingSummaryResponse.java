package gym.dto.response;

import java.time.LocalDate;

public class TrainerTrainingSummaryResponse {
    private final String trainingName;
    private final LocalDate trainingDate;
    private final String trainingType;
    private final int trainingDuration;
    private final String traineeName;

    public TrainerTrainingSummaryResponse(String trainingName, LocalDate trainingDate, String trainingType,
                                          int trainingDuration, String traineeName) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingType = trainingType;
        this.trainingDuration = trainingDuration;
        this.traineeName = traineeName;
    }

    public String getTrainingName() { return trainingName; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public String getTrainingType() { return trainingType; }
    public int getTrainingDuration() { return trainingDuration; }
    public String getTraineeName() { return traineeName; }
}