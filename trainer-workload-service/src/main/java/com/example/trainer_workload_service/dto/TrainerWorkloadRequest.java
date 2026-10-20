package com.example.trainer_workload_service.dto;

import com.example.trainer_workload_service.domain.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainerWorkloadRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private Boolean isActive;

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    @Positive
    private Double trainingDuration;

    @NotNull
    private ActionType actionType;
}
