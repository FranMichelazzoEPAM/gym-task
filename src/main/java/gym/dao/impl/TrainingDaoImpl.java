package gym.dao.impl;

import gym.dao.TrainingDao;
import gym.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TrainingDaoImpl implements TrainingDao {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrainingDaoImpl.class);
    private Map<UUID, Training> storage;

    @Autowired
    @Qualifier("trainingStorage")
    public void setStorage(Map<UUID, Training> storage) {
        this.storage = storage;
        LOG.info("Training storage injected (size={})", storage == null ? 0 : storage.size());
    }

    @Override
    public Training save(Training training) {
        // implementation to create a new training in the in-memory storage
        storage.put(training.getTrainingId(), training);
        LOG.info("Saved training: {} (id={})", training.getTrainingName(), training.getTrainingId());
        return training;
    }

    @Override
    public Optional<Training> findById(UUID id) {
        // implementation to select a training by ID
        LOG.debug("Looking for training with id={}", id);
        Training training = storage.get(id);
        if (training == null) {
            LOG.warn("Training not found: {}", id);
            return Optional.empty();
        }
        LOG.debug("Training found: {}", training.getTrainingName());
        return Optional.of(training);
    }

    @Override
    public List<Training> findAll() {
        // implementation to select all trainings from the in-memory storage
        LOG.debug("Finding all trainings (count={})", storage == null ? 0 : storage.size());
        return new ArrayList<>(storage.values());
    }
}
