package gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@ComponentScan("gym")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public Map<UUID, Trainee> traineeStorage() {
        return new HashMap<>();
    }

    @Bean
    public Map<UUID, Trainer> trainerStorage() {
        return new HashMap<>();
    }

    @Bean
    public Map<UUID, Training> trainingStorage() {
        return new HashMap<>();
    }
}
