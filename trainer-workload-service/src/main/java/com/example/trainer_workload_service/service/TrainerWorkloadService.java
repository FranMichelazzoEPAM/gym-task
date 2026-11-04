package com.example.trainer_workload_service.service;

import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.example.trainer_workload_service.dto.MonthlyWorkloadResponse;
import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;

public interface TrainerWorkloadService {
    void processWorkload(TrainerWorkloadRequest request);
    MonthlyWorkloadResponse getMonthlySummary(String username, int year, int month);
    TrainerWorkload getTrainerWorkload(String username);
}
