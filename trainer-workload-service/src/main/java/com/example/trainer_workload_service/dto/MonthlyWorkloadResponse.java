package com.example.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyWorkloadResponse {
    private String username;
    private int year;
    private int month;
    private double totalDuration;
}
