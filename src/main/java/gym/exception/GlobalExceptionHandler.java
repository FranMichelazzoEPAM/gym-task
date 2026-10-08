package gym.exception;

import gym.dto.response.ErrorResponse;
import gym.security.MissingCredentialsException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        meterRegistry.counter("gym.errors", "type", "bad_request").increment();

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        LOG.warn("Validation failed: {}", errors);

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        meterRegistry.counter("gym.errors", "type", "not_found").increment();
        LOG.error("Resource not found: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex) {
        meterRegistry.counter("gym.errors", "type", "unauthorized").increment();
        LOG.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MissingCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleMissingCredentials(MissingCredentialsException ex) {
        meterRegistry.counter("gym.errors", "type", "unauthorized").increment();
        LOG.warn("Missing/invalid credentials: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        meterRegistry.counter("gym.errors", "type", "bad_request").increment();
        LOG.warn("Invalid argument: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        meterRegistry.counter("gym.errors", "type", "bad_request").increment();
        LOG.warn("Malformed request body: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed request body",
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        meterRegistry.counter("gym.errors", "type", "general").increment();
        LOG.error("Unexpected error", ex);

        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        meterRegistry.counter("gym.errors", "type", "not_found").increment();
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        meterRegistry.counter("gym.errors", "type", "locked").increment();
        meterRegistry.counter("gym.login.attempts", "result", "failure").increment();
        LOG.warn("Account locked: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.LOCKED.value(),
                "Account locked due to multiple failed login attempts. Try again in 5 minutes.", null);
        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        meterRegistry.counter("gym.errors", "type", "unauthorized").increment();
        meterRegistry.counter("gym.login.attempts", "result", "failure").increment();
        LOG.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
