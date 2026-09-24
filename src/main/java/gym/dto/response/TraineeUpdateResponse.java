package gym.dto.response;

import java.time.LocalDate;
import java.util.List;

public class TraineeUpdateResponse {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String address;
    private final boolean active;
    private final List<TrainerSummaryResponse> trainers;

    public TraineeUpdateResponse(String username, String firstName, String lastName, LocalDate dateOfBirth,
                                 String address, boolean active, List<TrainerSummaryResponse> trainers) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.active = active;
        this.trainers = trainers;
    }

    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
    public List<TrainerSummaryResponse> getTrainers() { return trainers; }
}
