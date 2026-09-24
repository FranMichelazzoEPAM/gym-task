package gym.dto.response;

import java.time.LocalDate;
import java.util.List;

public class TraineeProfileResponse {
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String address;
    private final boolean isActive;
    private final List<TrainerSummaryResponse> trainers;

    public TraineeProfileResponse(String firstName,
                                  String lastName,
                                  LocalDate dateOfBirth,
                                  String address,
                                  boolean isActive,
                                  List<TrainerSummaryResponse> trainers) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
        this.trainers = trainers;
    }

    public String getFirstName() {
        return firstName;
    }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public boolean isActive() { return isActive; }
    public List<TrainerSummaryResponse> getTrainers() { return trainers; }

}
