package gym.config;

import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public MeterBinder activeTraineesGauge(TraineeRepository traineeRepository) {
        return registry -> Gauge.builder("gym.trainees.active", traineeRepository,
                repo -> repo.countByUser_IsActiveTrue())
                .description("Current number of active Trainees")
                .register(registry);
    }

    @Bean
    public MeterBinder activeTrainersGauge(TrainerRepository trainerRepository) {
        return registry -> Gauge.builder("gym.trainers.active", trainerRepository,
                repo -> repo.countByUser_IsActiveTrue())
                .description("Current number of active trainers")
                .register(registry);
    }
}