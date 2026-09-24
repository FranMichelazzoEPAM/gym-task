package gym.controller;

import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.dto.request.ToggleStatusTrainerRequest;
import gym.dto.request.TrainerRegistrationRequest;
import gym.dto.response.CredentialsResponse;
import gym.dto.response.TrainerProfileResponse;
import gym.dto.response.TrainerSummaryResponse;
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

        TrainingType trainingType =
                gymFacade.getTrainingTypeByName(request.getSpecialization());

        Trainer createdTrainer = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                trainingType
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

    // Get Trainer
    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    @ApiResponse(responseCode = "200", description = "Trainer found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<TrainerProfileResponse> getTrainer(
            Credentials caller,
            @PathVariable String username) {

        Trainer trainer = gymFacade.getTrainer(caller.username(), caller.password(), username);
        TrainerProfileResponse response = TrainerMapper.toProfileResponse(trainer);

        return ResponseEntity.ok(response);
    }


    // Get not assigned on trainee active trainers
    @GetMapping("/not-assigned-on-trainee/{username}")
    @Operation(summary = "Finds active trainers not already assigned to the given trainee")
    @ApiResponse(responseCode = "200", description = "Trainer(s) found")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<List<TrainerSummaryResponse>> getNotAssignedOnTraineeActiveTrainers(
            Credentials caller,
            @PathVariable String username) {

        List<Trainer> trainers = gymFacade.getTrainersNotAssignedToTrainee(
                caller.username(), caller.password(), username);

        List<TrainerSummaryResponse> response = trainers.stream()
                .map(TrainerMapper::toSummary)
                .toList();

        return ResponseEntity.ok(response);
    }

}