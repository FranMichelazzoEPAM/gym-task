package gym.dto.response;

import java.util.List;

public class TrainerUpdateResponse {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String specialization;
    private final boolean active;
    public final List<TraineeSummaryResponse> trainees;

    public TrainerUpdateResponse(String username, String firstName, String lastName,
                                 String specialization, boolean active, List<TraineeSummaryResponse> trainees) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.active = active;
        this.trainees = trainees;
    }

    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public boolean isActive() { return active; }
    public List<TraineeSummaryResponse> getTrainees() { return trainees; }
}
