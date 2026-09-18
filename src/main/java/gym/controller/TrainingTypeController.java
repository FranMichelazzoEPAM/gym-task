package gym.controller;

import gym.dto.response.TrainingTypeResponse;
import gym.facade.GymFacade;
import gym.mapper.TrainingTypeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Types")
public class TrainingTypeController {

    private final GymFacade gymFacade;

    public TrainingTypeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @GetMapping
    @Operation(summary = "List all available training types (specializations)")
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes() {
        List<TrainingTypeResponse> response = gymFacade.getAllTrainingTypes()
                .stream()
                .map(TrainingTypeMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}