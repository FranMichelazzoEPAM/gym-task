package gym.health.trainingType;

import gym.repository.TrainingTypeRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("trainingTypeData")
public class TrainingTypeDataHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeDataHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        long count = trainingTypeRepository.count();
        if (count == 0) {
            return Health.down()
                    .withDetail("reason", "No training types configured")
                    .build();
        }
        return Health.up()
                .withDetail("trainingTypesCount", count)
                .build();
    }

}
