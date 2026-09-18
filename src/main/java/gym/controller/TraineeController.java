package gym.controller;

import gym.domain.Trainee;
import gym.dto.request.TraineeRegistrationRequest;
import gym.dto.response.CredentialsResponse;
import gym.facade.GymFacade;
import gym.mapper.TraineeMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

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

        Trainee createdTrainee = gymFacade.createTrainee(
                request.getFirstName(),
                request.getLastName(),
                dateOfBirth,
                request.getAddress()
        );

        CredentialsResponse response = TraineeMapper.toCredentialsResponse(createdTrainee);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
