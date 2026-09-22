package gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class TrainerRegistrationRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotBlank(message = "Specialization is required")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String specialization;

    public TrainerRegistrationRequest() {
        // Required by Jackson
    }

    public TrainerRegistrationRequest(String firstName, String lastName, String specialization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
