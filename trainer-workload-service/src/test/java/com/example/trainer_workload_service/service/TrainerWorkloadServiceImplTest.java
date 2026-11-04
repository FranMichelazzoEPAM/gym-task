package com.example.trainer_workload_service.service;

import com.example.trainer_workload_service.domain.ActionType;
import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.example.trainer_workload_service.dto.MonthlyWorkloadResponse;
import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.example.trainer_workload_service.exception.TrainerNotFoundException;
import com.example.trainer_workload_service.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainerWorkloadServiceImplTest {

    private TrainerWorkloadRepository repository;
    private TrainerWorkloadServiceImpl service;
    private Map<String, TrainerWorkload> persistedTrainers;

    @BeforeEach
    void setUp() {
        repository = mock(TrainerWorkloadRepository.class);
        persistedTrainers = new HashMap<>();
        when(repository.findById(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(persistedTrainers.get(invocation.getArgument(0))));
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> {
            TrainerWorkload workload = invocation.getArgument(0);
            persistedTrainers.put(workload.getUsername(), workload);
            return workload;
        });
        service = new TrainerWorkloadServiceImpl(repository);
    }

    @Test
    void addsAndDeletesTrainingDurationWithoutDroppingBelowZero() {
        service.processWorkload(request("trainer1", "2025-03-10", 1.5, ActionType.ADD));
        service.processWorkload(request("trainer1", "2025-03-12", 0.5, ActionType.ADD));
        service.processWorkload(request("trainer1", "2025-03-15", 0.75, ActionType.DELETE));

        assertEquals(1.25, summary("trainer1", 2025, 3).getTotalDuration());

        service.processWorkload(request("trainer1", "2025-03-20", 2.0, ActionType.DELETE));

        assertEquals(0.0, summary("trainer1", 2025, 3).getTotalDuration());
    }

    @Test
    void groupsTrainingByMonthAndYearAndUpdatesTrainerDetails() {
        service.processWorkload(request("trainer1", "2025-03-10", 2.25, ActionType.ADD));
        service.processWorkload(request("trainer1", "2025-04-10", 1.0, ActionType.ADD));
        service.processWorkload(request("trainer1", "2026-03-10", 3.0, ActionType.ADD));
        TrainerWorkload trainer = persistedTrainers.get("trainer1");
        trainer.setFirstName("Old");
        trainer.setLastName("Name");
        trainer.setActive(false);

        service.processWorkload(request("trainer1", "2025-03-12", 0.25, ActionType.ADD));

        assertEquals(2.5, summary("trainer1", 2025, 3).getTotalDuration());
        assertEquals(1.0, summary("trainer1", 2025, 4).getTotalDuration());
        assertEquals(3.0, summary("trainer1", 2026, 3).getTotalDuration());
        assertEquals("First", trainer.getFirstName());
        assertEquals("Last", trainer.getLastName());
        assertTrue(trainer.isActive());
        assertEquals(2, trainer.getYears().get(0).getMonths().size());
        assertEquals(1, trainer.getYears().get(1).getMonths().size());
    }

    @Test
    void returnsZeroForUnrecordedMonthOrYear() {
        service.processWorkload(request("trainer1", "2025-03-10", 1.0, ActionType.ADD));

        assertEquals(0.0, summary("trainer1", 2025, 4).getTotalDuration());
        assertEquals(0.0, summary("trainer1", 2026, 3).getTotalDuration());
    }

    @Test
    void throwsWhenRequestingSummaryForUnknownTrainer() {
        assertThrows(TrainerNotFoundException.class, () -> service.getMonthlySummary("missing", 2025, 3));
    }

    @Test
    void firstWorkloadCreatesTrainerWithProfileAndWorkload() {
        service.processWorkload(request("trainer1", "2025-03-10", 1.0, ActionType.ADD));

        TrainerWorkload trainer = persistedTrainers.get("trainer1");
        assertEquals("trainer1", trainer.getUsername());
        assertEquals("First", trainer.getFirstName());
        assertEquals("Last", trainer.getLastName());
        assertTrue(trainer.isActive());
        assertFalse(trainer.getYears().isEmpty());
    }

    private MonthlyWorkloadResponse summary(String username, int year, int month) {
        return service.getMonthlySummary(username, year, month);
    }

    private TrainerWorkloadRequest request(String username, String date, double duration, ActionType action) {
        return new TrainerWorkloadRequest(
                username,
                "First",
                "Last",
                true,
                LocalDate.parse(date),
                duration,
                action);
    }
}
