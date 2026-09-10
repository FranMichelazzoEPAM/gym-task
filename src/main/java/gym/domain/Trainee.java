package gym.domain;

import java.util.Date;
import java.util.UUID;

public class Trainee extends User{
    private Date dateOfBirth;
    private String address;
    private UUID userId;

    public Trainee(String firstName, String lastName, String username, String password, boolean isActive, Date dateOfBirth, String address, UUID userId) {
        super(firstName, lastName, username, password, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.userId = userId;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String toString() {
        return
                super.toString() +
                ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                ", userId=" + userId;
    }
}
