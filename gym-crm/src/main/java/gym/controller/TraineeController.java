package gym.controller;

import gym.domain.Trainee;
import gym.domain.Training;
import gym.dto.request.ToggleStatusTraineeRequest;
import gym.dto.request.TraineeRegistrationRequest;
import gym.dto.request.TraineeTrainerListUpdateRequest;
import gym.dto.request.TraineeUpdateRequest;
import gym.dto.response.*;
import gym.facade.GymFacade;
import gym.mapper.TraineeMapper;
import gym.mapper.TrainingMapper;
import gym.service.result.TraineeRegistrationResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/trainees")
@Tag(name = "Trainee Management")
public class TraineeController {
    private final GymFacade gymFacade;

    public TraineeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    // Register a new trainee
    @PostMapping
    @Operation(summary = "Register a new trainee", description = "Creates a trainee profile a return generated credentials")
    @ApiResponse(responseCode = "200", description = "Trainee created successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid input data.")
    public ResponseEntity<CredentialsResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request) {

        Date dateOfBirth = TraineeMapper.toDate(request.getDateOfBirth());

        TraineeRegistrationResult createdTrainee = gymFacade.createTrainee(
                request.getFirstName(),
                request.getLastName(),
                dateOfBirth,
                request.getAddress()
        );

        CredentialsResponse response = TraineeMapper.toCredentialsResponse(
                createdTrainee.trainee(), createdTrainee.rawPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Delete Trainee
    @DeleteMapping("/{username}")
    @Operation(summary = "Delete a Trainee", description = "Deletes a trainee by username using the caller's basic auth credentials")
    @ApiResponse(responseCode = "204", description = "Trainee deleted successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or missing credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username) {
        gymFacade.deleteTrainee(username);
        return ResponseEntity.noContent().build();
    }

    // Activate/De-Activate Trainee
    @PatchMapping("/status")
    @Operation(summary = "Toggle Trainee status", description = "Changes the status of the Trainee to the given one")
    @ApiResponse(responseCode = "200", description = "Trainee's active status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Invalid or missing credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<Void> updateTraineeStatus(
            @Valid @RequestBody ToggleStatusTraineeRequest request) {

        gymFacade.updateTraineeActiveStatus(
                request.getUsername(),
                request.getActive());

        return ResponseEntity.ok().build();
    }

    // Get Trainee Profile
    @GetMapping("/{username}")
    @Operation(summary = "Get Trainee profile by username")
    @ApiResponse(responseCode = "200", description = "Trainee found")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<TraineeProfileResponse> getTrainee(
            @PathVariable String username) {

        Trainee trainee = gymFacade.getTrainee(username);
        TraineeProfileResponse response = TraineeMapper.toProfileResponse(trainee);
        return ResponseEntity.ok(response);
    }

    // Update Trainee
    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    @ApiResponse(responseCode = "200", description = "Trainee updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<TraineeUpdateResponse> updateTrainee(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request ) {

        Trainee existing = gymFacade.getTrainee(username);

        existing.getUser().setFirstName(request.getFirstName());
        existing.getUser().setLastName(request.getLastName());

        if (request.getDateOfBirth() != null) {
            existing.setDateOfBirth(TraineeMapper.toDate(request.getDateOfBirth()));
        }
        if (request.getAddress() != null) {
            existing.setAddress(request.getAddress());
        }

        Trainee updated = gymFacade.updateTrainee(existing);
        gymFacade.updateTraineeActiveStatus(username, request.getActive());

        if (updated.getUser().isActive() != request.getActive()) {
            updated.getUser().toggleActive();
        }

        TraineeUpdateResponse response = TraineeMapper.toUpdateResponse(updated);
        return ResponseEntity.ok(response);
    }

    // Update trainee's trainer list
    @PutMapping("/trainers-list/{username}")
    @Operation(summary = "Updates the Trainee trainers list by trainee username")
    @ApiResponse(responseCode = "200", description = "Trainee trainers list updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<TraineeTrainerListUpdateResponse> updateTraineeTrainersList(
            @PathVariable String username,
            @Valid @RequestBody TraineeTrainerListUpdateRequest request) {

        Trainee updated = gymFacade.updateTraineeTrainersList(
                username, request.getTrainersUsernames()
        );

        TraineeTrainerListUpdateResponse response =
                TraineeMapper.toTrainerListUpdateResponse(updated);

        return ResponseEntity.ok(response);
    }

    // Get Trainee trainings list
    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee's trainings list", description = "Returns a filtered list of a trainee's trainings")
    @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<List<TraineeTrainingSummaryResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        // Existence check — throws NoSuchElementException (mapped to 404) if trainee doesn't exist
        gymFacade.getTrainee(username);

        Date fromDate = TrainingMapper.toDate(periodFrom);
        Date toDate = TrainingMapper.toDate(periodTo);

        List<Training> trainings = gymFacade.getTraineeTrainings(
                username, fromDate, toDate, trainerName, trainingType);

        List<TraineeTrainingSummaryResponse> response = trainings.stream()
                .map(TrainingMapper::toSummary)
                .toList();

        return ResponseEntity.ok(response);
    }
}
