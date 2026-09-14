package gym.domain;

import jakarta.persistence.*;
import java.util.*;

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

    @ManyToMany
    @JoinTable(
            name = "trainer_specializations",
            joinColumns = @JoinColumn(name = "trainerId"),
            inverseJoinColumns = @JoinColumn(name = "trainingTypeId")
    )
    private List<TrainingType> specialization = new ArrayList<>();

    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees = new ArrayList<>();

    @OneToMany(mappedBy = "trainer")
    private List<Training> trainings = new ArrayList<>();

    public Trainer(User user, List<TrainingType> specialization) {
        this.user = user;
        this.specialization = specialization;
    }

    protected Trainer() {}

    public UUID getTrainerId() { return trainerId; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public List<TrainingType> getSpecialization() { return specialization; }

    public void setSpecialization(List<TrainingType> specialization) { this.specialization = specialization; }

    public List<Trainee> getTrainees() { return trainees; }

    public List<Training> getTrainings() { return trainings; }

    public String toString() {
        return "trainerId=" + trainerId + ", user=" + user +
                ", specialization=" + specialization;
    }
}