package gym;

import gym.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DataSeedIntegrationTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    void shouldSeedFiveTrainingTypes() {
        assertEquals(5, trainingTypeRepository.findAll().size());
    }
}