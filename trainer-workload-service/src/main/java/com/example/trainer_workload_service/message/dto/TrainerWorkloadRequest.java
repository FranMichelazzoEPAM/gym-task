package com.example.trainer_workload_service.message.dto;

import com.example.trainer_workload_service.domain.ActionType;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        LocalDate trainingDate,
        Double trainingDuration,
        ActionType actionType
) {}
