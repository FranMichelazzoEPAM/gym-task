package gym.controller;

import gym.client.TrainerWorkloadQueryClient;
import gym.client.dto.MonthlyWorkloadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainers")
public class TrainerWorkloadQueryController {

    private final TrainerWorkloadQueryClient client;

    public TrainerWorkloadQueryController(TrainerWorkloadQueryClient client) {
        this.client = client;
    }

    @GetMapping("/{username}/workload/{year}/{month}")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlySummary(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(client.getMonthlySummary(username, year, month));
    }
}