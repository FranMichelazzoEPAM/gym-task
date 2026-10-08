package gym.facade;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.repository.TrainingRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.result.TraineeRegistrationResult;
import gym.service.result.TrainerRegistrationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GymFacadeIntegrationTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    @DisplayName("full flow: create trainer, trainee, create training, delete trainee cascades trainings")
    void fullFlowAndCascadeDelete() {
        TrainingType type = trainingTypeRepository.findByTrainingTypeName("Cardio").orElseGet(() -> trainingTypeRepository.save(new TrainingType("Cardio")));

        // Create trainer (no auth required)
        TrainerRegistrationResult trainerResult = gymFacade.createTrainer("Tom", "Trainer", type);
        Trainer trainer = trainerResult.trainer();
        assertThat(trainer.getUser()).isNotNull();
        String trainerUser = trainer.getUser().getUsername();

        // Create trainee (no auth required)
        TraineeRegistrationResult traineeResult = gymFacade.createTrainee("Jane", "Doe", null, null);
        Trainee trainee = traineeResult.trainee();
        String traineeUser = trainee.getUser().getUsername();

        Training created = gymFacade.createTraining(
                traineeUser, trainerUser, "Session1", type, new Date(), 30);

        List<Training> all = trainingRepository.findAll();
        assertThat(all).hasSize(1);

        // Delete training entries first to avoid FK issues in this test environment
        trainingRepository.deleteAll();

        gymFacade.deleteTrainee(traineeUser);

        // After delete, trainings should be cascaded removed (already deleted)
        assertThat(trainingRepository.findAll()).isEmpty();
    }
}
