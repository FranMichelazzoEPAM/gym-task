package gym.service.impl;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public TrainingServiceImpl(TrainingRepository trainingRepository,
                               TraineeRepository traineeRepository,
                               TrainerRepository trainerRepository,
                               TrainingTypeRepository trainingTypeRepository) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        LOG.info("TrainingServiceImpl dependencies injected");
    }

    @Override
    @Transactional
    public Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                                   TrainingType trainingType, Date trainingDate, int trainingDuration) {
        LOG.debug("Creating training: {} for trainee={} trainer={}", trainingName, traineeUsername, trainerUsername);

        Trainee trainee = traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> {
                    LOG.error("Trainee not found: {}", traineeUsername);
                    return new NoSuchElementException("Trainee with username " + traineeUsername + " not found.");
                });

        Trainer trainer = trainerRepository.findByUser_Username(trainerUsername)
                .orElseThrow(() -> {
                    LOG.error("Trainer not found: {}", trainerUsername);
                    return new NoSuchElementException("Trainer with username " + trainerUsername + " not found.");
                });

        TrainingType managedType = trainingTypeRepository.findById(trainingType.getId())
                .orElseThrow(() -> {
                    LOG.error("Unknown training type id: {}", trainingType.getId());
                    return new NoSuchElementException("Training type not found: " + trainingType.getId());
                });

        Training training = new Training(trainee, trainer, trainingName, managedType, trainingDate, trainingDuration);
        trainingRepository.save(training);
        LOG.info("Created training: {} (id={})", trainingName, training.getTrainingId());
        return training;
    }

    @Override
    @Transactional(readOnly = true)
    public Training getTraining(UUID trainingId) {
        LOG.debug("Getting training: {}", trainingId);
        return trainingRepository.findById(trainingId).orElseThrow(() -> {
            LOG.error("Training not found: {}", trainingId);
            return new NoSuchElementException("Training with trainingId " + trainingId + " not found.");
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        LOG.debug("Getting all trainings");
        return trainingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername, Date fromDate, Date toDate,
                                              String trainerName, String trainingTypeName) {
        LOG.debug("Getting trainings for trainee {} with filters", traineeUsername);
        return trainingRepository.findTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername, Date fromDate, Date toDate,
                                              String traineeName) {
        LOG.debug("Getting trainings for trainer {} with filters", trainerUsername);
        return trainingRepository.findTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }
}