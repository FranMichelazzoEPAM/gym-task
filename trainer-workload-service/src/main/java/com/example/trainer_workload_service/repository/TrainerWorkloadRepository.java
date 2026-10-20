package com.example.trainer_workload_service.repository;

import com.example.trainer_workload_service.domain.TrainerWorkload;

import java.util.Optional;
import java.util.function.Supplier;

public interface TrainerWorkloadRepository {
    Optional<TrainerWorkload> findByUsername (String username);
    TrainerWorkload findOrCreate (String username, Supplier<TrainerWorkload> supplier);
    TrainerWorkload save(TrainerWorkload trainerWorkload);
    void deleteByUsername (String username);
}
