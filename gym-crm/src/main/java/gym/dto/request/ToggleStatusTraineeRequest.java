package gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ToggleStatusTraineeRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Active is required")
    private Boolean active;

    public ToggleStatusTraineeRequest() {
        // Required by Jackson
    }

    public ToggleStatusTraineeRequest(String username, Boolean active) {
        this.username = username;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
}
