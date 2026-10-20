package gym.controller;

import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.dto.request.ToggleStatusTrainerRequest;
import gym.dto.request.TrainerRegistrationRequest;
import gym.dto.request.TrainerUpdateRequest;
import gym.dto.response.*;
import gym.facade.GymFacade;
import gym.mapper.TrainerMapper;
import gym.mapper.TrainingMapper;
import gym.service.result.TrainerRegistrationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
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

        TrainerRegistrationResult createdTrainer = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                trainingType
        );

        CredentialsResponse response = TrainerMapper.toCredentialsResponse(
                createdTrainer.trainer(), createdTrainer.rawPassword());
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
            @Valid @RequestBody ToggleStatusTrainerRequest request) {

        gymFacade.updateTrainerActiveStatus(
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
            @PathVariable String username) {

        Trainer trainer = gymFacade.getTrainer(username);
        TrainerProfileResponse response = TrainerMapper.toProfileResponse(trainer);

        return ResponseEntity.ok(response);
    }

    //Update Trainer Profile
    @PutMapping("/{username}")
    @Operation(summary = "Updates a trainer by trainer's username")
    @ApiResponse(responseCode = "200", description = "Trainer updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<TrainerUpdateResponse> updateTrainer(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest request) {

        Trainer existing = gymFacade.getTrainer(username);
        TrainingType specialization = gymFacade.getTrainingTypeByName(request.getSpecialization());

        existing.getUser().setFirstName(request.getFirstName());
        existing.getUser().setLastName(request.getLastName());
        existing.setSpecialization(specialization);

        Trainer updatedTrainer = gymFacade.updateTrainer(existing);

        gymFacade.updateTrainerActiveStatus(username, request.isActive());

        if (updatedTrainer.getUser().isActive() != request.isActive()) {
            updatedTrainer.getUser().toggleActive();
        }

        TrainerUpdateResponse response = TrainerMapper.toUpdateResponse(updatedTrainer);
        return ResponseEntity.ok(response);
    }

    // Get not assigned on trainee active trainers
    @GetMapping("/not-assigned-on-trainee/{username}")
    @Operation(summary = "Finds active trainers not already assigned to the given trainee")
    @ApiResponse(responseCode = "200", description = "Trainer(s) found")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<List<TrainerSummaryResponse>> getNotAssignedOnTraineeActiveTrainers(
            @PathVariable String username) {

        List<Trainer> trainers = gymFacade.getTrainersNotAssignedToTrainee(username);

        List<TrainerSummaryResponse> response = trainers.stream()
                .map(TrainerMapper::toSummary)
                .toList();

        return ResponseEntity.ok(response);
    }

    // Get trainer trainings list
    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer's trainings list", description = "Returns a filtered list of a trainer's trainings")
    @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<List<TrainerTrainingSummaryResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {

        // Existence check — same pattern as the Trainee version, kept for consistency
        gymFacade.getTrainer(username);

        Date fromDate = TrainingMapper.toDate(periodFrom);
        Date toDate = TrainingMapper.toDate(periodTo);

        List<Training> trainings = gymFacade.getTrainerTrainings(
                username, fromDate, toDate, traineeName);

        List<TrainerTrainingSummaryResponse> response = trainings.stream()
                .map(TrainingMapper::toTrainerSummary)
                .toList();

        return ResponseEntity.ok(response);
    }
}