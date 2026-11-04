package gym.client;

import feign.RequestTemplate;
import gym.logging.TransactionIdFilter;
import gym.security.WorkloadServiceTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadQueryClientConfigTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void forwardsCurrentTransactionIdAndAddsServiceAuthorization() {
        String transactionId = "crm-tx-123";
        MDC.put(TransactionIdFilter.MDC_KEY, transactionId);
        WorkloadServiceTokenProvider tokenProvider =
                new WorkloadServiceTokenProvider("test-secret-key-that-is-at-least-32-bytes-long", 60_000);
        RequestTemplate template = new RequestTemplate();

        new TrainerWorkloadQueryClientConfig()
                .serviceAuthInterceptor(tokenProvider)
                .apply(template);

        assertTrue(template.headers().get(TransactionIdFilter.TX_ID_HEADER).contains(transactionId));
        assertTrue(template.headers().get("Authorization").stream()
                .anyMatch(value -> value.startsWith("Bearer ")));
    }
}
