package gym.dto.response;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String message;
    private final Map<String, String> errors;
    private final String transactionId = MDC.get("transactionId");

    public ErrorResponse(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, String> getErrors() { return errors; }
    public String getTransactionId() { return transactionId; }
}
