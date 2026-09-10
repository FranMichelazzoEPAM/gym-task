package gym.dao.impl;

import gym.dao.TraineeDao;
import gym.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TraineeDaoImpl implements TraineeDao {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TraineeDaoImpl.class);
    private Map<UUID, Trainee> storage;

    @Autowired
    @Qualifier("traineeStorage")
    public void setStorage(Map<UUID, Trainee> storage) {
        this.storage = storage;
        LOG.info("Trainee storage injected (size={})", storage == null ? 0 : storage.size());
    }

    @Override
    public List<Trainee> findAll(){
        // implementation to select all trainees from the in-memory storage
        LOG.debug("Finding all trainees (count={})", storage == null ? 0 : storage.size());
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Trainee> findById(UUID userId) {
        // implementation to select a trainee by ID
        LOG.debug("Looking for trainee with id={}", userId);
        Trainee trainee = storage.get(userId);
        if(trainee == null){
            LOG.warn("Trainee not found: {}", userId);
            return Optional.empty();
        }
        LOG.debug("Trainee found: {}", trainee.getUsername());
        return Optional.of(trainee);
    }

    @Override
    public Trainee save(Trainee trainee) {
        // implementation to save (create or update) a trainee in the in-memory storage
        storage.put(trainee.getUserId(), trainee);
        LOG.info("Saved trainee: {} (id={})", trainee.getUsername(), trainee.getUserId());
        return trainee;
    }

    @Override
    public void delete(UUID userId) {
        // implementation to delete a trainee
        storage.remove(userId);
        LOG.info("Deleted trainee with id={}", userId);
    }
}
