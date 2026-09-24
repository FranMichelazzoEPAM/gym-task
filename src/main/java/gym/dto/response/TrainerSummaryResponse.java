package gym.dto.response;

public class TrainerSummaryResponse {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String specialization;

    public TrainerSummaryResponse(String username, String firstName, String lastName, String specialization) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSpecialization() {
        return specialization;
    }
}
