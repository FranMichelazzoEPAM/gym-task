package gym.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrainerUpdateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialization is required")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String specialization;

    @NotNull(message = "Active status is required")
    private Boolean active;

    public TrainerUpdateRequest() {
        // Required by Jackson
    }

    public TrainerUpdateRequest(String firstName, String lastName, String specialization, Boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.active = active;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public Boolean isActive() { return active; }
}
