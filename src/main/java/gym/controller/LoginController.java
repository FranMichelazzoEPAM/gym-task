package gym.controller;

import gym.dto.request.ChangePasswordRequest;
import gym.facade.GymFacade;
import gym.security.Credentials;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class LoginController {
    private final GymFacade gymFacade;
    private final MeterRegistry meterRegistry;

    public LoginController(GymFacade gymFacade, MeterRegistry meterRegistry) {
        this.gymFacade = gymFacade;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/login")
    @Operation(summary = "Validate credentials")
    @ApiResponse(responseCode = "200", description = "Credentials are valid")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Void> login(Credentials caller) {
        boolean authenticated = gymFacade.authenticateTrainee(caller.username(), caller.password())
                || gymFacade.authenticateTrainer(caller.username(), caller.password());

        meterRegistry.counter("gym.login.attempts", "result", authenticated ? "success" : "failure")
                .increment();

        if (!authenticated) {
            throw new SecurityException("Authentication failed for user: " + caller.username());
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes a user's password given their current password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Old password does not match")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        gymFacade.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
