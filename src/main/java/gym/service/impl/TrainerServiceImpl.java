package gym.service.impl;

import gym.dao.TrainerDao;
import gym.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import gym.service.PasswordGenerationService;
import gym.service.TrainerService;
import gym.service.UsernameGenerationService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrainerServiceImpl.class);
    private TrainerDao trainerDao;
    private UsernameGenerationService usernameGenerationService;
    private PasswordGenerationService passwordGenerationService;

    @Autowired
    @Qualifier("trainerDaoImpl")
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
        LOG.info("TrainerDao injected");
    }

    @Autowired
    public void setUsernameGenerationService(UsernameGenerationService usernameGenerationService) {
        this.usernameGenerationService = usernameGenerationService;
        LOG.info("UsernameGenerationService injected into TrainerService");
    }

    @Autowired
    public void setPasswordGenerationService(PasswordGenerationService passwordGenerationService) {
        this.passwordGenerationService = passwordGenerationService;
        LOG.info("PasswordGenerationService injected into TrainerService");
    }

    @Override
    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        // Implementation for creating a new trainer
        LOG.debug("Creating trainer: {} {}", firstName, lastName);
        UUID userId = UUID.randomUUID();
        String username = usernameGenerationService.generateUsername(firstName, lastName);
        String password = passwordGenerationService.generateRandomPassword();
        boolean isActive = true;

        Trainer trainer = new Trainer(firstName, lastName, username, password, isActive, userId, specialization);
        trainerDao.save(trainer);
        LOG.info("Created trainer: {} (id={})", username, userId);

        return trainer;
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        // Implementation for updating an existing trainer
        LOG.debug("Updating trainer: {}", trainer.getUserId());
        if (trainerDao.findById(trainer.getUserId()).isPresent()) {
            trainerDao.save(trainer);
            LOG.info("Updated trainer: {}", trainer.getUserId());
            return trainer;
        } else {
            LOG.error("Trainer not found for update: {}", trainer.getUserId());
            throw new NoSuchElementException("Trainer with userId " + trainer.getUserId() + " not found.");
        }
    }

    @Override
    public Trainer getTrainer(UUID userId) {
        // Implementation for retrieving a trainer by userId
        LOG.debug("Getting trainer: {}", userId);
        return trainerDao.findById(userId).orElseThrow(() -> {
            LOG.error("Trainer not found: {}", userId);
            return new NoSuchElementException("Trainer with userId " + userId + " not found.");
        });
    }

    @Override
    public List<Trainer> getAllTrainers() {
        // Implementation for retrieving all trainers
        LOG.debug("Getting all trainers");
        return trainerDao.findAll();
    }
}
