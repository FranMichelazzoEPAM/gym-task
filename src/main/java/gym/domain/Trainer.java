package gym.domain;

import java.util.UUID;

public class Trainer extends User {
    private UUID userId;
    private String specialization;

    public Trainer(String firstName, String lastName, String username, String password, boolean isActive, UUID userId, String specialization) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.specialization = specialization;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String toString() {
        return
                super.toString() +
                ", userId=" + userId +
                ", specialization=" + specialization;
    }
}
