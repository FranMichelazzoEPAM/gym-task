package gym.service.impl;

import gym.dao.TrainingDao;
import gym.domain.Training;
import gym.domain.TrainingType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TrainingServiceImplTest {

    @Test
    public void testCreateTrainingSavesAndReturns() {
        TrainingDao dao = Mockito.mock(TrainingDao.class);
        when(dao.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingServiceImpl service = new TrainingServiceImpl();
        service.setTrainingDao(dao);

        UUID traineeId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();
        Training result = service.createTraining(traineeId, trainerId, "Leg Day", new TrainingType("strength"), new Date(), Duration.ofMinutes(45));

        Assertions.assertNotNull(result.getTrainingId());
        Assertions.assertEquals("Leg Day", result.getTrainingName());
        verify(dao, times(1)).save(any(Training.class));
    }

    @Test
    public void testGetTrainingNotFoundThrows() {
        TrainingDao dao = Mockito.mock(TrainingDao.class);
        UUID id = UUID.randomUUID();
        when(dao.findById(id)).thenReturn(Optional.empty());

        TrainingServiceImpl service = new TrainingServiceImpl();
        service.setTrainingDao(dao);

        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> service.getTraining(id));
    }
}
