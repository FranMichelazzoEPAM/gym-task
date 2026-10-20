package gym.service;

import gym.client.TrainerWorkloadNotifier;
import gym.client.dto.ActionType;
import gym.client.dto.TrainerWorkloadRequest;
import gym.domain.*;
import gym.exception.TrainingDeletionNotAllowedException;
import gym.repository.*;
import gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

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

    @Mock
    private TrainerWorkloadNotifier trainerWorkloadNotifier;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType type;

    @BeforeEach
    void setUp() {
        trainee = new Trainee(new User("Trainee", "One", "t1", "p", true), null, null);
        trainer = new Trainer(new User("Trainer", "One", "tr", "p", true), new TrainingType("Cardio"));
        type = new TrainingType("Cardio");
    }

    @Test
    @DisplayName("createTraining succeeds when all refs exist")
    void createTrainingSuccess() {
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("tr")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(isNull())).thenReturn(Optional.of(type));
        when(trainingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Date trainingDate = new Date();
        Training created = trainingService.createTraining("t1", "tr", "name", type, trainingDate, 45);

        assertThat(created.getTrainingName()).isEqualTo("name");
        verify(trainerWorkloadNotifier).notifyWorkload(new TrainerWorkloadRequest(
                "tr", "Trainer", "One", true,
                trainingDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                0.75, ActionType.ADD));
    }

    @Test
    @DisplayName("createTraining fails when trainee missing")
    void createTrainingFailsTraineeMissing() {
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.createTraining("t1", "tr", "n", type, new Date(), 10));
        verifyNoInteractions(trainerWorkloadNotifier);
    }

    @Test
    @DisplayName("createTraining fails when trainer missing")
    void createTrainingFailsTrainerMissing() {
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("tr")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.createTraining("t1", "tr", "n", type, new Date(), 10));
        verifyNoInteractions(trainerWorkloadNotifier);
    }

    @Test
    @DisplayName("createTraining fails when type missing")
    void createTrainingFailsTypeMissing() {
        when(traineeRepository.findByUser_Username("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_Username("tr")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(isNull())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.createTraining("t1", "tr", "n", type, new Date(), 10));
        verifyNoInteractions(trainerWorkloadNotifier);
    }

    @Test
    @DisplayName("deleteTraining deletes a future training and notifies the workload service")
    void deleteTrainingFutureTraining() {
        UUID trainingId = UUID.randomUUID();
        Date futureDate = new Date(System.currentTimeMillis() + 86_400_000);
        Training training = new Training(trainee, trainer, "name", type, futureDate, 90);
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(trainingId);

        verify(trainingRepository).delete(training);
        verify(trainerWorkloadNotifier).notifyWorkload(new TrainerWorkloadRequest(
                "tr", "Trainer", "One", true,
                futureDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                1.5, ActionType.DELETE));
    }

    @Test
    @DisplayName("deleteTraining rejects a training that is not in the future")
    void deleteTrainingRejectsPastTraining() {
        UUID trainingId = UUID.randomUUID();
        Training training = new Training(trainee, trainer, "name", type, new Date(0), 90);
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));

        assertThrows(TrainingDeletionNotAllowedException.class, () -> trainingService.deleteTraining(trainingId));

        verify(trainingRepository, never()).delete(any());
        verifyNoInteractions(trainerWorkloadNotifier);
    }

    @Test
    @DisplayName("deleteTraining fails when training is missing")
    void deleteTrainingFailsWhenMissing() {
        UUID trainingId = UUID.randomUUID();
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.deleteTraining(trainingId));

        verify(trainingRepository, never()).delete(any());
        verifyNoInteractions(trainerWorkloadNotifier);
    }
}
