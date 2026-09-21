package gym.security;

public class MissingCredentialsException extends RuntimeException {
    public MissingCredentialsException(String message) {
        super(message);
    }
}
