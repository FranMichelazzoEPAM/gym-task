package gym.health.trainer;

import gym.repository.TrainerRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("activeTrainers")
public class ActiveTrainerHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;

    public ActiveTrainerHealthIndicator(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public Health health() {
        long activeTrainers = trainerRepository.countByUser_IsActiveTrue();

        if (activeTrainers == 0) {
            return Health.down()
                    .withDetail("reason", "No active trainers available")
                    .build();
        }
        return Health.up().withDetail("activeTrainers", activeTrainers).build();
    }
}
