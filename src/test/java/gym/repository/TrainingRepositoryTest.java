package gym.repository;

import gym.domain.*;
import gym.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("save training and query by trainee username")
    void saveAndQueryByTrainee() {
        TrainingType type = trainingTypeRepository.findByTrainingTypeName("Cardio").orElseGet(() -> trainingTypeRepository.save(TestDataFactory.trainingType("Cardio")));
        Trainer trainer = trainerRepository.save(TestDataFactory.trainer("Mark","T","muser","p",true));
        Trainee trainee = traineeRepository.save(TestDataFactory.trainee("Lily","P","lily","pw",true));

        Training tr = new Training(trainee, trainer, "Morning", type, new Date(), 40);
        trainingRepository.save(tr);

        List<Training> byTrainee = trainingRepository.findTraineeTrainings("lily", null, null, null, null);
        assertThat(byTrainee).hasSize(1);
    }

    @Test
    @DisplayName("query trainer trainings by date and name filter")
    void queryTrainerTrainings() {
        TrainingType type = trainingTypeRepository.findByTrainingTypeName("Yoga").orElseGet(() -> trainingTypeRepository.save(TestDataFactory.trainingType("Yoga")));
        Trainer trainer = trainerRepository.save(TestDataFactory.trainer("Sam","H","sam","pw",true));
        Trainee trainee = traineeRepository.save(TestDataFactory.trainee("Nina","Q","nina","pw",true));

        Date now = new Date();
        Training tr = new Training(trainee, trainer, "Evening", type, now, 50);
        trainingRepository.save(tr);

        List<Training> results = trainingRepository.findTrainerTrainings("sam", null, null, "Nina");
        assertThat(results).hasSize(1);
    }
}
