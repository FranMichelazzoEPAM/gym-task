package gym.dto.response;

import java.util.List;

public class TrainerProfileResponse {
    private final String firstName;
    private final String lastName;
    private final String specialization;
    private final boolean isActive;
    private final List<TraineeSummaryResponse> trainees;

    public TrainerProfileResponse(String firstName,
                                  String lastName,
                                  String specialization,
                                  boolean isActive,
                                  List<TraineeSummaryResponse> trainees) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.isActive = isActive;
        this.trainees = trainees;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public boolean isActive() { return isActive; }
    public List<TraineeSummaryResponse> getTrainees() { return trainees; }
}
