package gym.client;

import gym.client.dto.TrainerWorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WorkloadNotificationEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(WorkloadNotificationEventListener.class);

    private final JmsTemplate jmsTemplate;
    private final String queueName;

    public WorkloadNotificationEventListener(
            JmsTemplate jmsTemplate,
            @Value("${app.messaging.trainer-workload-queue}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkloadNotification(WorkloadNotificationEvent event) {
        TrainerWorkloadRequest request = event.request();
        try {
            jmsTemplate.convertAndSend(queueName, request, message -> {
                message.setJMSType("trainerWorkloadMessage");
                return message;
            });
        } catch (JmsException ex) {
            LOG.error("Failed to publish workload notification for trainer={} action={} | reason={}",
                    request.username(), request.actionType(), ex.getMessage());
        }
    }
}