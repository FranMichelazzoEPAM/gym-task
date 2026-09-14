package gym.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    private UUID traineeId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", nullable = false, unique = true)
    private User user;

    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date dateOfBirth;

    @Column(nullable = true)
    private String address;

    @ManyToMany
    @JoinTable(
            name = "trainee_trainers",
            joinColumns = @JoinColumn(name = "traineeId"),
            inverseJoinColumns = @JoinColumn(name = "trainerId")
    )
    private List<Trainer> trainers = new ArrayList<>();

    // Note 7: hard delete → cascade to trainings
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();

    public Trainee(User user, Date dateOfBirth, String address) {
        this.user = user;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    protected Trainee() {
        // Required by JPA
    }

    public UUID getTraineeId() { return traineeId; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Date getDateOfBirth() { return dateOfBirth; }

    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public List<Trainer> getTrainers() { return trainers; }

    public void setTrainers(List<Trainer> trainers) { this.trainers = trainers; }

    public List<Training> getTrainings() { return trainings; }

    public String toString() {
        return "traineeId=" + traineeId + ", user=" + user +
                ", dateOfBirth=" + dateOfBirth + ", address='" + address + '\'';
    }
}