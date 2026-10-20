package gym.client;

import feign.RequestInterceptor;
import gym.logging.TransactionIdFilter;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;

public class FeignClientConfig {

    private final ServiceTokenProvider tokenProvider;

    public FeignClientConfig(ServiceTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = tokenProvider.generateServiceToken();
            requestTemplate.header("Authorization", "Bearer " + token);

            String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);
            if (transactionId != null) {
                requestTemplate.header(TransactionIdFilter.TX_ID_HEADER, transactionId);
            }
        };
    }
}
