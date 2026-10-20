package com.example.trainer_workload_service.exception;

public class TrainerNotFoundException extends RuntimeException {
    public TrainerNotFoundException(String username) {
        super("Trainer not found: " + username);
    }
}
