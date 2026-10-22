package gym.client.message;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadProducer {

    private final JmsTemplate jmsTemplate;

    @Value("${app.messaging.trainer-workload-queue}")
    private String queueName;

    public void publishWorkloadUpdate(TrainerWorkloadMessage message) {
        try {
            jmsTemplate.convertAndSend(queueName, message, m -> {
                m.setJMSType("trainerWorkloadMessage");
                return m;
            });
            log.info("Published workload update for trainer {}", message.getTrainerUsername());
        } catch (JmsException ex) {
            log.error("Failed to publish workload message, broker unreachable", ex);
            throw ex;
        }
    }
}
