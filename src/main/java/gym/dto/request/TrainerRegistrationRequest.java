package gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class TrainerRegistrationRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotEmpty(message = "At least one specialization is required")
    private List<@NotBlank(message = "Specialization name must not be blank") String> specializations;

    public TrainerRegistrationRequest() {
        // Required by Jackson
    }

    public TrainerRegistrationRequest(String firstName, String lastName, List<String> specializations) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specializations = specializations;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public List<String> getSpecializations() { return specializations; }
    public void setSpecializations(List<String> specialization) { this.specializations = specialization; }
}
