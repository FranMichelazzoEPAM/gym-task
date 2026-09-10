package gym.service.impl;

import gym.dao.TraineeDao;
import gym.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import gym.service.PasswordGenerationService;
import gym.service.TraineeService;

import gym.service.UsernameGenerationService;

import java.util.*;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TraineeServiceImpl.class);
    private TraineeDao traineeDao;
    private UsernameGenerationService usernameGenerationService;
    private PasswordGenerationService passwordGenerationService;

    @Autowired
    @Qualifier("traineeDaoImpl")
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
        LOG.info("TraineeDao injected");
    }

    @Autowired
    public void setUsernameGenerationService(UsernameGenerationService usernameGenerationService) {
        this.usernameGenerationService = usernameGenerationService;
        LOG.info("UsernameGenerationService injected");
    }

    @Autowired
    public void setPasswordGenerationService(PasswordGenerationService passwordGenerationService) {
        this.passwordGenerationService = passwordGenerationService;
        LOG.info("PasswordGenerationService injected");
    }

    @Override
    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        // Implementation for creating a new trainee
        LOG.debug("Creating trainee: {} {}", firstName, lastName);
        UUID userId = UUID.randomUUID();
        String username = usernameGenerationService.generateUsername(firstName, lastName);
        String password = passwordGenerationService.generateRandomPassword();
        boolean isActive = true;
        Trainee trainee = new Trainee(firstName, lastName, username, password, isActive, dateOfBirth, address, userId);
        traineeDao.save(trainee);
        LOG.info("Created trainee: {} (id={})", username, userId);
        return trainee;
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        // Implementation for updating an existing trainee
        LOG.debug("Updating trainee: {}", trainee.getUserId());
        if (traineeDao.findById(trainee.getUserId()).isPresent()) {
            traineeDao.save(trainee);
            LOG.info("Updated trainee: {}", trainee.getUserId());
            return trainee;
        } else {
            LOG.error("Trainee not found for update: {}", trainee.getUserId());
        throw new NoSuchElementException("Trainee with userId " + trainee.getUserId() + " not found.");
        }
    }

    @Override
    public void deleteTrainee(UUID userId) {
        // Implementation for deleting a trainee by userId
        LOG.debug("Deleting trainee: {}", userId);
        if (traineeDao.findById(userId).isPresent()) {
            traineeDao.delete(userId);
            LOG.info("Deleted trainee: {}", userId);
        } else {
            LOG.error("Trainee not found for delete: {}", userId);
            throw new NoSuchElementException("Trainee with userId " + userId + " not found.");
        }
    }

    @Override
    public Trainee getTrainee(UUID userId) {
        // Implementation for retrieving a trainee by userId
        LOG.debug("Getting trainee: {}", userId);
        return traineeDao.findById(userId).orElseThrow(() -> {
            LOG.error("Trainee not found: {}", userId);
            return new NoSuchElementException("Trainee with userId " + userId + " not found.");
        });
    }

    @Override
    public List<Trainee> getAllTrainees() {
        // Implementation for retrieving all trainees
        LOG.debug("Getting all trainees");
        return traineeDao.findAll();
    }
}
