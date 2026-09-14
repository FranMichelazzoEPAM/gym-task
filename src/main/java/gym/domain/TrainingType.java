package gym.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "training_types")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String trainingTypeName;

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    protected TrainingType() {
        // Required by JPA
    }

    public UUID getId() { return id; }

    public String getTrainingTypeName() { return trainingTypeName; }
    // No setter — values are fixed constants, not app-editable (Note 12)

    public String toString() {
        return "id=" + id + ", trainingTypeName='" + trainingTypeName + '\'';
    }
}