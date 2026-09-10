package gym.domain;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

public class Training {
    private UUID trainingId;
    private UUID traineeId;
    private UUID trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private Date trainingDate;
    private Duration trainingDuration;

    public Training(UUID trainingId, UUID traineeId, UUID trainerId, String trainingName, TrainingType trainingType, Date trainingDate, Duration trainingDuration) {
        this.trainingId = trainingId;
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public UUID getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(UUID trainingId) {this.trainingId = trainingId;}

    public UUID getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(UUID traineeId) {
        this.traineeId = traineeId;
    }

    public UUID getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(UUID trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public Date getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(Date trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Duration getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Duration trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public String toString() {
        return
                "trainingId=" + trainingId +
                ", traineeId=" + traineeId +
                ", trainerId=" + trainerId +
                ", trainingName='" + trainingName + '\'' +
                ", trainingType='" + trainingType + '\'' +
                ", trainingDate=" + trainingDate +
                ", trainingDuration=" + trainingDuration;
    }
}
