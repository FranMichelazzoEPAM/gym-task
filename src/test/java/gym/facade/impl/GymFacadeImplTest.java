package gym.facade.impl;

import gym.domain.*;
import gym.facade.GymFacade;
import gym.service.TraineeService;
import gym.service.TrainerService;
import gym.service.TrainingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GymFacadeImplTest {

    @Test
    public void testFacadeDelegatesForTrainee() {
        TraineeService traineeService = Mockito.mock(TraineeService.class);
        TrainerService trainerService = Mockito.mock(TrainerService.class);
        TrainingService trainingService = Mockito.mock(TrainingService.class);

        Trainee t = new Trainee("F","L","u","p",true,new Date(),"a", UUID.randomUUID());
        when(traineeService.createTrainee(any(), any(), any(), any())).thenReturn(t);
        when(traineeService.getTrainee(t.getUserId())).thenReturn(t);
        when(traineeService.getAllTrainees()).thenReturn(Arrays.asList(t));

        GymFacadeImpl facade = new GymFacadeImpl(traineeService, trainerService, trainingService);

        Trainee created = facade.createTrainee("F","L", new Date(), "a");
        Assertions.assertEquals(t, created);

        Trainee gotten = facade.getTrainee(t.getUserId());
        Assertions.assertEquals(t, gotten);

        List<Trainee> all = facade.getAllTrainees();
        Assertions.assertEquals(1, all.size());
        verify(traineeService, times(1)).createTrainee(any(), any(), any(), any());
        verify(traineeService, times(1)).getTrainee(t.getUserId());
        verify(traineeService, times(1)).getAllTrainees();
    }

    @Test
    public void testFacadeDelegatesForTrainerAndTraining() {
        TraineeService traineeService = Mockito.mock(TraineeService.class);
        TrainerService trainerService = Mockito.mock(TrainerService.class);
        TrainingService trainingService = Mockito.mock(TrainingService.class);

        UUID id = UUID.randomUUID();
        Trainer tr = new Trainer("T","S","u","p",true,id, "spec");
        when(trainerService.createTrainer(any(), any(), any())).thenReturn(tr);
        when(trainerService.getTrainer(id)).thenReturn(tr);

        UUID trainingId = UUID.randomUUID();
        Training tg = new Training(trainingId, UUID.randomUUID(), id, "Run", new TrainingType("cardio"), new Date(), Duration.ofMinutes(30));
        when(trainingService.createTraining(any(), any(), any(), any(), any(), any())).thenReturn(tg);
        when(trainingService.getTraining(trainingId)).thenReturn(tg);
        when(trainingService.getAllTrainings()).thenReturn(Arrays.asList(tg));

        GymFacadeImpl facade = new GymFacadeImpl(traineeService, trainerService, trainingService);

        Trainer createdTrainer = facade.createTrainer("T","S","spec");
        Assertions.assertEquals(tr, createdTrainer);

        Trainer gottenTrainer = facade.getTrainer(id);
        Assertions.assertEquals(tr, gottenTrainer);

        Training createdTraining = facade.createTraining(UUID.randomUUID(), id, "Run", new TrainingType("cardio"), new Date(), Duration.ofMinutes(30));
        Assertions.assertEquals(tg, createdTraining);

        Training gottenTraining = facade.getTraining(trainingId);
        Assertions.assertEquals(tg, gottenTraining);

        List<Training> all = facade.getAllTrainings();
        Assertions.assertEquals(1, all.size());

        verify(trainerService, times(1)).createTrainer(any(), any(), any());
        verify(trainerService, times(1)).getTrainer(id);
        verify(trainingService, times(1)).createTraining(any(), any(), any(), any(), any(), any());
        verify(trainingService, times(1)).getTraining(trainingId);
        verify(trainingService, times(1)).getAllTrainings();
    }
}
