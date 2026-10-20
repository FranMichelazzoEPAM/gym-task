package gym.domain;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private UUID trainerId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "trainingTypeId", nullable = false)
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees = new ArrayList<>();

    @OneToMany(mappedBy = "trainer")
    private List<Training> trainings = new ArrayList<>();

    public Trainer(User user, TrainingType specialization) {
        this.user = user;
        this.specialization = specialization;
    }

    protected Trainer() {}

    public UUID getTrainerId() { return trainerId; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public TrainingType getSpecialization() { return specialization; }

    public void setSpecialization(TrainingType specialization) { this.specialization = specialization; }

    public List<Trainee> getTrainees() { return trainees; }

    public List<Training> getTrainings() { return trainings; }

    public String toString() {
        return "trainerId=" + trainerId + ", user=" + user +
                ", specialization=" + specialization;
    }
}