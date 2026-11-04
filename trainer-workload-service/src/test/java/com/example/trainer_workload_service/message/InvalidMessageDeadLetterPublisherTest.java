package com.example.trainer_workload_service.message;

import com.example.trainer_workload_service.domain.ActionType;
import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.core.JmsTemplate;
import jakarta.jms.Message;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvalidMessageDeadLetterPublisherTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    void sendsOriginalRequestAndValidationErrorsToConfiguredDeadLetterQueue() throws Exception {
        String dlqName = "trainer.workload.invalid.dlq";
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                null,
                "First",
                "Last",
                true,
                LocalDate.of(2025, 3, 10),
                1.5,
                ActionType.ADD);
        List<String> errors = List.of("username: must not be blank");
        InvalidMessageDeadLetterPublisher publisher =
                new InvalidMessageDeadLetterPublisher(jmsTemplate, dlqName);

        publisher.publish(request, errors, "tx-dead-letter");

        ArgumentCaptor<InvalidMessageEnvelope> envelopeCaptor =
                ArgumentCaptor.forClass(InvalidMessageEnvelope.class);
        ArgumentCaptor<MessagePostProcessor> processorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(dlqName), envelopeCaptor.capture(), processorCaptor.capture());
        InvalidMessageEnvelope envelope = envelopeCaptor.getValue();
        Message deadLetterMessage = mock(Message.class);
        processorCaptor.getValue().postProcessMessage(deadLetterMessage);
        assertAll(
                () -> assertEquals("tx-dead-letter", envelope.transactionId()),
                () -> assertEquals(request, envelope.originalMessage()),
                () -> assertEquals(errors, envelope.validationErrors()),
                () -> assertNotNull(envelope.failedAt()),
                () -> org.mockito.Mockito.verify(deadLetterMessage)
                        .setStringProperty("transactionId", "tx-dead-letter"));
    }
}
