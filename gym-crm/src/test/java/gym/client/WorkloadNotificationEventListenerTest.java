package gym.client;

import gym.client.dto.ActionType;
import gym.client.dto.TrainerWorkloadRequest;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadNotificationEventListenerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    void sendsWorkloadNotificationToConfiguredQueueWithExpectedMessageType() throws Exception {
        String queueName = "trainer.workload.queue";
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "trainer1",
                "First",
                "Last",
                true,
                LocalDate.of(2025, 3, 10),
                1.5,
                ActionType.ADD);
        WorkloadNotificationEventListener listener =
                new WorkloadNotificationEventListener(jmsTemplate, queueName);

        listener.onWorkloadNotification(new WorkloadNotificationEvent(request));

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(queueName), eq(request), postProcessorCaptor.capture());

        Message message = mock(Message.class);
        assertSame(message, postProcessorCaptor.getValue().postProcessMessage(message));
        verify(message).setJMSType("trainerWorkloadMessage");
    }
}
