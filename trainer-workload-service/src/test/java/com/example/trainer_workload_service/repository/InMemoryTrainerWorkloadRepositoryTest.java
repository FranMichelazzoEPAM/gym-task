package com.example.trainer_workload_service.repository;

import com.example.trainer_workload_service.domain.TrainerWorkload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTrainerWorkloadRepositoryTest {

    private InMemoryTrainerWorkloadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrainerWorkloadRepository();
    }

    @Test
    void findsMissingAndSavedTrainers() {
        assertTrue(repository.findByUsername("trainer1").isEmpty());

        TrainerWorkload trainer = trainer("trainer1");
        repository.save(trainer);

        assertSame(trainer, repository.findByUsername("trainer1").orElseThrow());
    }

    @Test
    void createsTrainerOnlyOnceForUsername() {
        AtomicInteger supplierCalls = new AtomicInteger();
        TrainerWorkload first = repository.findOrCreate("trainer1", () -> {
            supplierCalls.incrementAndGet();
            return trainer("trainer1");
        });

        TrainerWorkload second = repository.findOrCreate("trainer1", () -> {
            supplierCalls.incrementAndGet();
            return trainer("trainer1");
        });

        assertSame(first, second);
        assertEquals(1, supplierCalls.get());
    }

    @Test
    void deletingTrainerDoesNotRemoveOtherTrainers() {
        TrainerWorkload first = trainer("trainer1");
        TrainerWorkload second = trainer("trainer2");
        repository.save(first);
        repository.save(second);

        repository.deleteByUsername("trainer1");

        assertFalse(repository.findByUsername("trainer1").isPresent());
        assertSame(second, repository.findByUsername("trainer2").orElseThrow());
    }

    private TrainerWorkload trainer(String username) {
        return new TrainerWorkload(username, "First", "Last", true);
    }
}
