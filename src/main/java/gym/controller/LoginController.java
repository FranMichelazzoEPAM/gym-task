package gym.controller;

import gym.facade.GymFacade;
import gym.security.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class LoginController {
    private final GymFacade gymFacade;

    public LoginController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @GetMapping("/login")
    @Operation(summary = "Validate credentials")
    @ApiResponse(responseCode = "200", description = "Credentials are valid")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Void> login(Credentials caller) {
        boolean authenticated = gymFacade.authenticateTrainee(caller.username(), caller.password())
                || gymFacade.authenticateTrainer(caller.username(), caller.password());

        if (!authenticated) {
            throw new SecurityException("Authentication failed for user: " + caller.username());
        }

        return ResponseEntity.ok().build();
    }
}
