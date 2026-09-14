package gym.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private UUID trainingId;

    @ManyToOne
    @JoinColumn(name = "traineeId", nullable = false)
    private Trainee trainee;

    @ManyToOne
    @JoinColumn(name = "trainerId", nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private String trainingName;

    @ManyToOne
    @JoinColumn(name = "trainingTypeId", nullable = false)
    private TrainingType trainingType;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date trainingDate;

    @Column(nullable = false)
    private int trainingDuration; // duration in minutes

    public Training(Trainee trainee, Trainer trainer, String trainingName,
                    TrainingType trainingType, Date trainingDate, int trainingDuration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    protected Training() {
        // Required by JPA
    }

    public UUID getTrainingId() { return trainingId; }

    public Trainee getTrainee() { return trainee; }

    public void setTrainee(Trainee trainee) { this.trainee = trainee; }

    public Trainer getTrainer() { return trainer; }

    public void setTrainer(Trainer trainer) { this.trainer = trainer; }

    public String getTrainingName() { return trainingName; }

    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingType getTrainingType() { return trainingType; }

    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public Date getTrainingDate() { return trainingDate; }

    public void setTrainingDate(Date trainingDate) { this.trainingDate = trainingDate; }

    public int getTrainingDuration() { return trainingDuration; }

    public void setTrainingDuration(int trainingDuration) { this.trainingDuration = trainingDuration; }

    public String toString() {
        return "trainingId=" + trainingId + ", trainee=" + trainee + ", trainer=" + trainer +
                ", trainingName='" + trainingName + '\'' + ", trainingType=" + trainingType +
                ", trainingDate=" + trainingDate + ", trainingDuration=" + trainingDuration;
    }
}