package gym.dao.impl;

import gym.dao.TrainerDao;
import gym.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TrainerDaoImpl implements TrainerDao {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrainerDaoImpl.class);
    private Map<UUID, Trainer> storage;

    @Autowired
    @Qualifier("trainerStorage")
    public void setStorage(Map<UUID, Trainer> storage) {
        this.storage = storage;
        LOG.info("Trainer storage injected (size={})", storage == null ? 0 : storage.size());
    }

    @Override
    public List<Trainer> findAll() {
        // implementation to select all trainers from the in-memory storage
        LOG.debug("Finding all trainers (count={})", storage == null ? 0 : storage.size());
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Trainer> findById(UUID userId) {
        // implementation to select a trainer by ID
        LOG.debug("Looking for trainer with id={}", userId);
        Trainer trainer = storage.get(userId);
        if (trainer == null){
            LOG.warn("Trainer not found: {}", userId);
            return Optional.empty();
        }
        LOG.debug("Trainer found: {}", trainer.getUsername());
        return Optional.of(trainer);
    }

    @Override
    public Trainer save(Trainer trainer) {
        // implementation to create a new trainer in the in-memory storage
        storage.put(trainer.getUserId(), trainer);
        LOG.info("Saved trainer: {} (id={})", trainer.getUsername(), trainer.getUserId());
        return trainer;
    }

}
