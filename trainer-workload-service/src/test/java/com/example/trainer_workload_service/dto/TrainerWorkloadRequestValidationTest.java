package com.example.trainer_workload_service.dto;

import com.example.trainer_workload_service.domain.ActionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void acceptsCompleteRequestWithFractionalDuration() {
        assertTrue(validator.validate(validRequest()).isEmpty());
    }

    @Test
    void requiresAllFieldsAndRejectsNonPositiveDuration() {
        TrainerWorkloadRequest missingFields = new TrainerWorkloadRequest();
        Set<String> missingProperties = validator.validate(missingFields).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "username",
                "firstName",
                "lastName",
                "isActive",
                "trainingDate",
                "trainingDuration",
                "actionType"), missingProperties);

        TrainerWorkloadRequest zeroDuration = validRequest();
        zeroDuration.setTrainingDuration(0.0);
        assertTrue(validator.validate(zeroDuration).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("trainingDuration")));

        TrainerWorkloadRequest negativeDuration = validRequest();
        negativeDuration.setTrainingDuration(-1.0);
        assertTrue(validator.validate(negativeDuration).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("trainingDuration")));
    }

    @Test
    void rejectsBlankRequiredTextFields() {
        TrainerWorkloadRequest request = validRequest();
        request.setUsername(" ");
        request.setFirstName("");
        request.setLastName("  ");

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(invalidProperties.containsAll(Set.of("username", "firstName", "lastName")));
    }

    private static TrainerWorkloadRequest validRequest() {
        return new TrainerWorkloadRequest(
                "trainer1", "First", "Last", true,
                LocalDate.of(2025, 3, 10), 0.5, ActionType.ADD);
    }
}
