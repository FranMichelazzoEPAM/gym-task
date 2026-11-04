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
import org.slf4j.MDC;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        org.mockito.Mockito.doAnswer(invocation -> {
            assertEquals("tx-123", MDC.get("transactionId"));
            return null;
        }).when(service).processWorkload(request);

        consumer.onMessage(request, "tx-123");

        verify(service).processWorkload(request);
        verifyNoInteractions(deadLetterPublisher);
        assertNull(MDC.get("transactionId"));
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

        consumer.onMessage(request, "tx-invalid");

        verify(deadLetterPublisher).publish(eq(request), argThat(errors ->
                errors.stream().anyMatch(error -> error.startsWith("username:"))), eq("tx-invalid"));
        verifyNoInteractions(service);
    }

    @Test
    void propagatesProcessingFailureSoMessageCanBeRedelivered() {
        TrainerWorkloadRequest request = validRequest();
        RuntimeException processingFailure = new IllegalStateException("processing failed");
        doThrow(processingFailure).when(service).processWorkload(request);

        assertThrows(IllegalStateException.class, () -> consumer.onMessage(request, "tx-failure"));

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
