package gym.client.message;

import gym.logging.TransactionIdFilter;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadProducerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void addsCurrentTransactionIdToPublishedJmsMessage() throws Exception {
        String transactionId = "crm-tx-456";
        MDC.put(TransactionIdFilter.MDC_KEY, transactionId);
        TrainerWorkloadProducer producer = new TrainerWorkloadProducer(jmsTemplate);
        ReflectionTestUtils.setField(producer, "queueName", "trainer.workload.queue");
        TrainerWorkloadMessage workloadMessage = new TrainerWorkloadMessage();

        producer.publishWorkloadUpdate(workloadMessage);

        ArgumentCaptor<MessagePostProcessor> processorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(
                eq("trainer.workload.queue"), eq(workloadMessage), processorCaptor.capture());
        Message jmsMessage = mock(Message.class);
        processorCaptor.getValue().postProcessMessage(jmsMessage);
        verify(jmsMessage).setStringProperty("transactionId", transactionId);
        verify(jmsMessage).setJMSType("trainerWorkloadMessage");
    }
}
