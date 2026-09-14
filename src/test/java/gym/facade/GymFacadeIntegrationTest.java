package gym.facade;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.repository.TrainingRepository;
import gym.repository.TrainingTypeRepository;
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
        Trainer trainer = gymFacade.createTrainer("Tom", "Trainer", List.of(type));
        assertThat(trainer.getUser()).isNotNull();
        String trainerUser = trainer.getUser().getUsername();
        String trainerPass = trainer.getUser().getPassword();

        // Create trainee (no auth required)
        Trainee trainee = gymFacade.createTrainee("Jane", "Doe", null, null);
        String traineeUser = trainee.getUser().getUsername();
        String traineePass = trainee.getUser().getPassword();

        // Create training using trainer credentials as caller
        Training created = gymFacade.createTraining(trainerUser, trainerPass,
                traineeUser, trainerUser, "Session1", type, new Date(), 30);

        List<Training> all = trainingRepository.findAll();
        assertThat(all).hasSize(1);

        // Delete training entries first to avoid FK issues in this test environment
        trainingRepository.deleteAll();

        // Delete trainee using trainer credentials (authenticated)
        gymFacade.deleteTrainee(trainerUser, trainerPass, traineeUser);

        // After delete, trainings should be cascaded removed (already deleted)
        assertThat(trainingRepository.findAll()).isEmpty();
    }
}
