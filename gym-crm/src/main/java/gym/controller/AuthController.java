package gym.controller;

import gym.dto.request.ChangePasswordRequest;
import gym.dto.response.LoginResponse;
import gym.facade.GymFacade;
import gym.security.Credentials;
import gym.security.jwt.JwtUtil;
import gym.security.userDetails.CustomUserDetails;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GymFacade gymFacade;
    private final MeterRegistry meterRegistry;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil, GymFacade gymFacade, MeterRegistry meterRegistry) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.gymFacade = gymFacade;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain a JWT")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "423", description = "Account locked due to repeated login attempts")
    public ResponseEntity<LoginResponse> login(Credentials caller) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(caller.username(), caller.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtUtil.generateToken(userDetails.getUsername(), role);

        meterRegistry.counter("gym.login.attempts", "result", "success").increment();

        return ResponseEntity.ok(new LoginResponse(token));
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
