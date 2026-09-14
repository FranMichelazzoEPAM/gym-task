package gym.repository;

import gym.domain.TrainingType;
import gym.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainingTypeRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("save and find by name")
    void saveAndFindByName() {
        TrainingType type = trainingTypeRepository.save(TestDataFactory.trainingType("Cross"));
        Optional<TrainingType> found = trainingTypeRepository.findByTrainingTypeName("Cross");
        assertThat(found).isPresent();
        assertThat(found.get().getTrainingTypeName()).isEqualTo("Cross");
    }
}
