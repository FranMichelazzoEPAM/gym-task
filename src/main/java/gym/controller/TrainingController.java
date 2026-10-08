package gym.controller;

import gym.domain.TrainingType;
import gym.dto.request.TrainingRegistrationRequest;
import gym.facade.GymFacade;
import gym.mapper.TrainingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings management")
public class TrainingController {

    private final GymFacade gymFacade;

    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(summary = "Creates a new training", description = "Creates a new training for a trainee and trainer")
    @ApiResponse(responseCode = "200", description = "Training created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody TrainingRegistrationRequest request) {

        TrainingType trainingType = gymFacade.getTrainer(
                request.getTrainerUsername())
                .getSpecialization();

        Date trainingDate = TrainingMapper.toDate(request.getDate());

        gymFacade.createTraining(
                request.getTraineeUsername(),
                request.getTrainerUsername(),
                request.getName(),
                trainingType,
                trainingDate,
                request.getDuration());

        return ResponseEntity.ok().build();
    }
}
