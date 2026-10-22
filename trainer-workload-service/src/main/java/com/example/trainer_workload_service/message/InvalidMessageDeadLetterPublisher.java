package com.example.trainer_workload_service.message;


import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class InvalidMessageDeadLetterPublisher {

    private final JmsTemplate jmsTemplate;
    private final String dlqName;

    public InvalidMessageDeadLetterPublisher(
            JmsTemplate jmsTemplate,
            @Value("${app.messaging.trainer-workload-invalid-dlq}") String dlqName) {
        this.jmsTemplate = jmsTemplate;
        this.dlqName = dlqName;
    }

    public void publish(TrainerWorkloadRequest original, List<String> errors) {
        InvalidMessageEnvelope envelope = new InvalidMessageEnvelope(original, errors, Instant.now());
        jmsTemplate.convertAndSend(dlqName, envelope);
    }
}
