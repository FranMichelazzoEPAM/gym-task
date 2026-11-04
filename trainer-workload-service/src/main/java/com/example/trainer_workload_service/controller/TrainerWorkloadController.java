package com.example.trainer_workload_service.controller;

import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.example.trainer_workload_service.dto.MonthlyWorkloadResponse;
import com.example.trainer_workload_service.service.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/trainers")
@Tag(name = "Trainer Workload", description = "Retrieve monthly workload summaries")
public class TrainerWorkloadController {

    private final TrainerWorkloadService service;

    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    @GetMapping("/{username}/workload/{year}/{month}")
    @Operation(summary = "Get a trainer's monthly workload summary")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlySummary(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(service.getMonthlySummary(username, year, month));
    }

    @GetMapping("/{username}/workload")
    @Operation(summary = "Get a trainer's complete workload summary")
    public ResponseEntity<TrainerWorkload> getTrainerWorkload(
            @PathVariable String username) {
        return ResponseEntity.ok(service.getTrainerWorkload(username));
    }
}
