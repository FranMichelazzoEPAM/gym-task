package com.example.trainer_workload_service.message;

import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;

import java.time.Instant;
import java.util.List;

public record InvalidMessageEnvelope(
        TrainerWorkloadRequest originalMessage,
        List<String> validationErrors,
        Instant failedAt
) { }
