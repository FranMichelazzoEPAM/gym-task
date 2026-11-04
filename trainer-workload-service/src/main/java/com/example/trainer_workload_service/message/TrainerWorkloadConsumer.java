package com.example.trainer_workload_service.message;

import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.example.trainer_workload_service.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TrainerWorkloadConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TrainerWorkloadConsumer.class);
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final Pattern SAFE_TRANSACTION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

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
    public void onMessage(
            TrainerWorkloadRequest request,
            @Header(name = TRANSACTION_ID_PROPERTY, required = false) String incomingTransactionId) {
        String transactionId = safeTransactionId(incomingTransactionId);
        String previousTransactionId = MDC.get("transactionId");
        MDC.put("transactionId", transactionId);
        long start = System.nanoTime();
        String outcome = "failed";

        try {
            Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);

            if (!violations.isEmpty()) {
                List<String> errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.toList());

                LOG.warn("Invalid workload message rejected; violationCount={}", errors.size());
                deadLetterPublisher.publish(request, errors, transactionId);
                outcome = "rejected";
                return;
            }

            service.processWorkload(request);
            outcome = "processed";
        } finally {
            LOG.info("TRANSACTION | transactionId={} type=jms operation=processWorkload outcome={} durationMs={}",
                    transactionId, outcome, (System.nanoTime() - start) / 1_000_000);
            if (previousTransactionId == null) {
                MDC.remove("transactionId");
            } else {
                MDC.put("transactionId", previousTransactionId);
            }
        }
    }

    private static String safeTransactionId(String transactionId) {
        return transactionId != null && SAFE_TRANSACTION_ID.matcher(transactionId).matches()
                ? transactionId
                : UUID.randomUUID().toString();
    }
}
