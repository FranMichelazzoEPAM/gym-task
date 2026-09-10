package gym.service;

import gym.dao.TraineeDao;
import gym.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UsernameGenerationService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UsernameGenerationService.class);
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    @Qualifier("traineeDaoImpl")
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
        LOG.info("TraineeDao injected into UsernameGenerationService");
    }

    @Autowired
    @Qualifier("trainerDaoImpl")
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
        LOG.info("TrainerDao injected into UsernameGenerationService");
    }

    public String generateUsername(String firstName, String lastName) {
        // Implementation for generating a username based on first name and last name checking for uniqueness
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;
        LOG.debug("Generating username for {} {}. base={}", firstName, lastName, baseUsername);

        while (usernameExists(username)) {
            username = baseUsername + suffix;
            LOG.debug("Username collision, trying {}", username);
            suffix++;
        }
        LOG.info("Generated username={}", username);
        return username;
    }

    private boolean usernameExists(String username) {
        // Check if the username exists in either trainee or trainer storage
        boolean exists = traineeDao.findAll().stream().anyMatch(t -> t.getUsername().equals(username)) ||
               trainerDao.findAll().stream().anyMatch(t -> t.getUsername().equals(username));
        if (exists) LOG.debug("Username exists: {}", username);
        return exists;
    }
}
