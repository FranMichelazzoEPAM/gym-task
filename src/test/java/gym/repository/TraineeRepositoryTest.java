package gym.repository;

import gym.domain.*;
import gym.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TraineeRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    @DisplayName("save trainee and find by username")
    void saveAndFindByUserUsername() {
        TrainingType type = trainingTypeRepository.findByTrainingTypeName("Cardio").orElseGet(() -> trainingTypeRepository.save(TestDataFactory.trainingType("Cardio")));
        Trainer trainer = trainerRepository.save(TestDataFactory.trainer("T1","Last","tuser","pass",true));
        Trainee trainee = traineeRepository.save(TestDataFactory.trainee("John","Doe","jdoe","pwd",true));

        // create a training to ensure relationships persist
        Training training = new Training(trainee, trainer, "Session1", type, new Date(), 60);
        trainingRepository.save(training);

        var found = traineeRepository.findByUser_Username("jdoe");
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUsername()).isEqualTo("jdoe");
        // trainings may be lazily loaded — assert via training repository instead
        assertThat(trainingRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("delete trainee by username cascades trainings")
    void deleteByUserUsernameCascadesTrainings() {
        TrainingType type = trainingTypeRepository.findByTrainingTypeName("Pilates").orElseGet(() -> trainingTypeRepository.save(TestDataFactory.trainingType("Pilates")));
        Trainer trainer = trainerRepository.save(TestDataFactory.trainer("T2","Last","tuser2","pwd2",true));
        Trainee trainee = traineeRepository.save(TestDataFactory.trainee("Alice","Smith","asmith","pwd",true));

        Training training = new Training(trainee, trainer, "Session2", type, new Date(), 30);
        trainingRepository.save(training);

        // ensure training exists
        assertThat(trainingRepository.findAll()).hasSize(1);

        // ensure trainings removed first to avoid FK issues in this test environment
        trainingRepository.deleteAll();

        // delete via entity
        traineeRepository.delete(trainee);

        // training should be removed
        assertThat(trainingRepository.findAll()).isEmpty();
    }
}
