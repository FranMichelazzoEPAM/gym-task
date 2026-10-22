package com.example.trainer_workload_service.message;

import com.example.trainer_workload_service.domain.ActionType;
import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.example.trainer_workload_service.service.TrainerWorkloadService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadConsumerTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Mock
    private TrainerWorkloadService service;

    @Mock
    private InvalidMessageDeadLetterPublisher deadLetterPublisher;

    private TrainerWorkloadConsumer consumer;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        consumer = new TrainerWorkloadConsumer(service, validator, deadLetterPublisher);
    }

    @Test
    void processesValidWorkloadRequest() {
        TrainerWorkloadRequest request = validRequest();

        consumer.onMessage(request);

        verify(service).processWorkload(request);
        verifyNoInteractions(deadLetterPublisher);
    }

    @Test
    void publishesInvalidRequestAndValidationErrorsToDeadLetterPublisher() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                null,
                "First",
                "Last",
                true,
                LocalDate.of(2025, 3, 10),
                1.5,
                ActionType.ADD);

        consumer.onMessage(request);

        verify(deadLetterPublisher).publish(eq(request), argThat(errors ->
                errors.stream().anyMatch(error -> error.startsWith("username:"))));
        verifyNoInteractions(service);
    }

    @Test
    void propagatesProcessingFailureSoMessageCanBeRedelivered() {
        TrainerWorkloadRequest request = validRequest();
        RuntimeException processingFailure = new IllegalStateException("processing failed");
        doThrow(processingFailure).when(service).processWorkload(request);

        assertThrows(IllegalStateException.class, () -> consumer.onMessage(request));

        verify(service).processWorkload(request);
        verifyNoInteractions(deadLetterPublisher);
    }

    private TrainerWorkloadRequest validRequest() {
        return new TrainerWorkloadRequest(
                "trainer1",
                "First",
                "Last",
                true,
                LocalDate.of(2025, 3, 10),
                1.5,
                ActionType.ADD);
    }
}
