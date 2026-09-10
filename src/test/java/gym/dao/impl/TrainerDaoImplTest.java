package gym.dao.impl;

import gym.domain.Trainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TrainerDaoImplTest {

    @Test
    public void testSaveAndFindAllAndFindById() {
        TrainerDaoImpl dao = new TrainerDaoImpl();
        Map<UUID, Trainer> storage = new HashMap<>();
        dao.setStorage(storage);

        Assertions.assertTrue(dao.findAll().isEmpty());

        UUID id = UUID.randomUUID();
        Trainer tr = new Trainer("Jane","Smith","jane.smith","pw",true,id,"yoga");
        dao.save(tr);

        List<Trainer> all = dao.findAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals(tr, all.get(0));

        Optional<Trainer> found = dao.findById(id);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(tr, found.get());
    }

    @Test
    public void testFindByIdEmpty() {
        TrainerDaoImpl dao = new TrainerDaoImpl();
        dao.setStorage(new HashMap<>());
        Assertions.assertFalse(dao.findById(UUID.randomUUID()).isPresent());
    }
}
