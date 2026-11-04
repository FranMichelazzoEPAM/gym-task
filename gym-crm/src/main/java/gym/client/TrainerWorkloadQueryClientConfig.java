package gym.client;

import feign.RequestInterceptor;
import gym.logging.TransactionIdFilter;
import gym.security.WorkloadServiceTokenProvider;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrainerWorkloadQueryClientConfig {

    @Bean
    public RequestInterceptor serviceAuthInterceptor(WorkloadServiceTokenProvider tokenProvider) {
        return template -> {
            template.header("Authorization", "Bearer " + tokenProvider.generateToken());
            String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);
            if (transactionId != null) {
                template.header(TransactionIdFilter.TX_ID_HEADER, transactionId);
            }
        };
    }
}