package gym.controller;

import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.dto.request.ToggleStatusTraineeRequest;
import gym.dto.request.ToggleStatusTrainerRequest;
import gym.dto.request.TrainerRegistrationRequest;
import gym.dto.response.CredentialsResponse;
import gym.facade.GymFacade;
import gym.mapper.TrainerMapper;
import gym.security.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer Management")
public class TrainerController {

    private final GymFacade gymFacade;

    public TrainerController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(summary = "Register a new trainer", description = "Creates a trainer profile and returns generated credentials")
    @ApiResponse(responseCode = "201", description = "Trainer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Specialization not found")
    public ResponseEntity<CredentialsResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {

        List<TrainingType> trainingTypes = request.getSpecializations()
                .stream()
                .map(gymFacade::getTrainingTypeByName)
                .toList();

        Trainer createdTrainer = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                trainingTypes
        );

        CredentialsResponse response = TrainerMapper.toCredentialsResponse(createdTrainer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Activate/De-Activate Trainer
    @PatchMapping("/status")
    @Operation(summary = "Toggle Trainer status", description = "Changes the status of the Trainer to the given one")
    @ApiResponse(responseCode = "200", description = "Trainer's active status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Invalid or missing credentials")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<Void> updateTrainerStatus(
            @Valid @RequestBody ToggleStatusTrainerRequest request,
            Credentials caller) {

        gymFacade.updateTrainerActiveStatus(caller.username(),
                caller.password(),
                request.getUsername(),
                request.getActive());

        return ResponseEntity.ok().build();
    }
}