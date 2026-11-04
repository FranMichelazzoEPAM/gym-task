package gym.client.message;

import gym.logging.TransactionIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadProducer {

    private final JmsTemplate jmsTemplate;

    @Value("${app.messaging.trainer-workload-queue}")
    private String queueName;

    public void publishWorkloadUpdate(TrainerWorkloadMessage message) {
        String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        try {
            String messageTransactionId = transactionId;
            jmsTemplate.convertAndSend(queueName, message, m -> {
                m.setJMSType("trainerWorkloadMessage");
                m.setStringProperty("transactionId", messageTransactionId);
                return m;
            });
            log.info("Published workload update | transactionId={} operation=publishWorkloadUpdate",
                    messageTransactionId);
        } catch (JmsException ex) {
            log.error("Failed to publish workload message | exceptionType={}", ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
