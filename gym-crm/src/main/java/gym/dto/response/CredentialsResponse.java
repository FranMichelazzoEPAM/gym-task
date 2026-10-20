package gym.dto.response;

public class CredentialsResponse {
    private final String username;
    private final String password;

    public CredentialsResponse(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
