package gym.dto.response;

import java.util.UUID;

public class TrainingTypeResponse {
    private final UUID id;
    private final String name;

    public TrainingTypeResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
