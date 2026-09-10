package gym.dao.impl;

import gym.domain.Training;
import gym.domain.TrainingType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;

public class TrainingDaoImplTest {

    @Test
    public void testSaveAndFindAndFindAll() {
        TrainingDaoImpl dao = new TrainingDaoImpl();
        Map<UUID, Training> storage = new HashMap<>();
        dao.setStorage(storage);

        UUID trainingId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();
        Training t = new Training(trainingId, traineeId, trainerId, "Leg Day", new TrainingType("strength"), new Date(), Duration.ofMinutes(60));

        dao.save(t);

        Optional<Training> found = dao.findById(trainingId);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(t, found.get());

        List<Training> all = dao.findAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals(t, all.get(0));
    }

    @Test
    public void testFindByIdEmpty() {
        TrainingDaoImpl dao = new TrainingDaoImpl();
        dao.setStorage(new HashMap<>());
        Assertions.assertFalse(dao.findById(UUID.randomUUID()).isPresent());
    }
}
