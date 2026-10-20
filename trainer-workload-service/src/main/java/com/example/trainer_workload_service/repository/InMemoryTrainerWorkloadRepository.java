package com.example.trainer_workload_service.repository;

import com.example.trainer_workload_service.domain.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Repository
public class InMemoryTrainerWorkloadRepository implements TrainerWorkloadRepository {

    private final Map<String, TrainerWorkload> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        return Optional.ofNullable(storage.get(username));
    }

    @Override
    public TrainerWorkload findOrCreate(String username, Supplier<TrainerWorkload> supplier) {
        return storage.computeIfAbsent(username, key -> supplier.get());
    }

    @Override
    public TrainerWorkload save(TrainerWorkload trainerWorkload) {
        storage.put(trainerWorkload.getUsername(), trainerWorkload);
        return trainerWorkload;
    }

    @Override
    public void deleteByUsername(String username) {
        storage.remove(username);
    }
}
