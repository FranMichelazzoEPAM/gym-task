package com.example.trainer_workload_service.message;

import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.example.trainer_workload_service.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TrainerWorkloadConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TrainerWorkloadConsumer.class);

    private final TrainerWorkloadService service;
    private final Validator validator;
    private final InvalidMessageDeadLetterPublisher deadLetterPublisher;

    public TrainerWorkloadConsumer(TrainerWorkloadService service,
                                   Validator validator,
                                   InvalidMessageDeadLetterPublisher deadLetterPublisher) {
        this.service = service;
        this.validator = validator;
        this.deadLetterPublisher = deadLetterPublisher;
    }

    @JmsListener(
            destination = "${app.messaging.trainer-workload-queue}",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void onMessage(TrainerWorkloadRequest request) {
        Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            List<String> errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toList());

            LOG.warn("Invalid workload message rejected: {}", errors);
            deadLetterPublisher.publish(request, errors);
            return; // normal return -> transaction commits -> message removed from main queue
        }

        // Let any exception here propagate -> transaction rolls back -> broker redelivers
        service.processWorkload(request);
    }

}
