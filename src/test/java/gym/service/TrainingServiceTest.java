package gym.service;

import gym.domain.*;
import gym.repository.*;
import gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    @DisplayName("createTraining succeeds when all refs exist")
    void createTrainingSuccess() {
        Trainee trainee = new Trainee(new User("F","L","t1","p",true), null, null);
        Trainer trainer = new Trainer(new User("F","L","tr","p",true), List.of());
        TrainingType type = new TrainingType("Cardio");

        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("tr")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(any())).thenReturn(Optional.of(type));
        when(trainingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Training created = trainingService.createTraining("t1","tr","name", type, new Date(), 45);
        assertThat(created.getTrainingName()).isEqualTo("name");
    }

    @Test
    @DisplayName("createTraining fails when trainee missing")
    void createTrainingFailsTraineeMissing() {
        TrainingType type = new TrainingType("Cardio");
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.createTraining("t1","tr","n", type, new Date(), 10));
    }

    @Test
    @DisplayName("createTraining fails when trainer missing")
    void createTrainingFailsTrainerMissing() {
        Trainee trainee = new Trainee(new User("F","L","t1","p",true), null, null);
        TrainingType type = new TrainingType("Cardio");
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("tr")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.createTraining("t1","tr","n", type, new Date(), 10));
    }

    @Test
    @DisplayName("createTraining fails when type missing")
    void createTrainingFailsTypeMissing() {
        Trainee trainee = new Trainee(new User("F","L","t1","p",true), null, null);
        Trainer trainer = new Trainer(new User("F","L","tr","p",true), List.of());
        TrainingType type = new TrainingType("Cardio");
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("tr")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.createTraining("t1","tr","n", type, new Date(), 10));
    }
}
