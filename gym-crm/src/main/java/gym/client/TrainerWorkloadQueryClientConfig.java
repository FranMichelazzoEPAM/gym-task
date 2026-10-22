package gym.client;

import gym.security.WorkloadServiceTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;

@Configuration
public class TrainerWorkloadQueryClientConfig {

    @Bean
    public RequestInterceptor serviceAuthInterceptor(WorkloadServiceTokenProvider tokenProvider) {
        return template -> template.header("Authorization", "Bearer " + tokenProvider.generateToken());
    }
}