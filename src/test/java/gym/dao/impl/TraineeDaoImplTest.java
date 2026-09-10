package gym.dao.impl;

import gym.domain.Trainee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TraineeDaoImplTest {

    @Test
    public void testFindAllAndSaveAndDelete() {
        TraineeDaoImpl dao = new TraineeDaoImpl();
        Map<UUID, Trainee> storage = new HashMap<>();
        dao.setStorage(storage);

        Assertions.assertTrue(dao.findAll().isEmpty());

        UUID id = UUID.randomUUID();
        Trainee t = new Trainee("John", "Doe", "john.doe", "pass", true, new Date(), "addr", id);
        dao.save(t);

        List<Trainee> all = dao.findAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals(t, all.get(0));

        Optional<Trainee> found = dao.findById(id);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(t, found.get());

        dao.delete(id);
        Assertions.assertFalse(dao.findById(id).isPresent());
    }

    @Test
    public void testFindByIdEmpty() {
        TraineeDaoImpl dao = new TraineeDaoImpl();
        dao.setStorage(new HashMap<>());
        Assertions.assertFalse(dao.findById(UUID.randomUUID()).isPresent());
    }
}
