package gym.exception;

public class TrainingDeletionNotAllowedException extends RuntimeException {
    public TrainingDeletionNotAllowedException(String message) {
        super(message);
    }
}
