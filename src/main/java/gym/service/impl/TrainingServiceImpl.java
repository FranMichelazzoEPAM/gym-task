package gym.service.impl;

import gym.dao.TrainingDao;
import gym.domain.Training;
import gym.domain.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import gym.service.TrainingService;

import java.time.Duration;
import java.util.*;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrainingServiceImpl.class);
    private TrainingDao trainingDao;

    @Autowired
    @Qualifier("trainingDaoImpl")
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
        LOG.info("TrainingDao injected");
    }

    @Override
    public Training createTraining(UUID traineeId, UUID trainerId, String trainingName, TrainingType trainingType, Date trainingDate, Duration trainingDuration) {
        // Implementation for creating a new training
        LOG.debug("Creating training: {} for trainee={} trainer={}", trainingName, traineeId, trainerId);
        UUID trainingId = UUID.randomUUID();
        Training training = new Training(trainingId, traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration);
        trainingDao.save(training);
        LOG.info("Created training: {} (id={})", trainingName, trainingId);
        return training;
    }

    @Override
    public Training getTraining(UUID trainingId) {
        // Implementation for retrieving a training by trainingId
        LOG.debug("Getting training: {}", trainingId);
        return trainingDao.findById(trainingId).orElseThrow(() -> {
            LOG.error("Training not found: {}", trainingId);
            return new NoSuchElementException("Training with trainingId " + trainingId + " not found.");
        });
    }

    @Override
    public List<Training> getAllTrainings() {
        // Implementation for retrieving all trainings
        LOG.debug("Getting all trainings");
        return trainingDao.findAll();
    }

}
