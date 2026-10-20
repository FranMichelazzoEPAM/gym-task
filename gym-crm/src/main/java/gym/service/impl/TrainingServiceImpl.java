package gym.service.impl;

import gym.client.TrainerWorkloadNotifier;
import gym.client.dto.ActionType;
import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.exception.TrainingDeletionNotAllowedException;
import gym.mapper.TrainerWorkloadRequestMapper;
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
    private final TrainerWorkloadNotifier trainerWorkloadNotifier;

    @Autowired
    public TrainingServiceImpl(TrainingRepository trainingRepository,
                               TraineeRepository traineeRepository,
                               TrainerRepository trainerRepository,
                               TrainingTypeRepository trainingTypeRepository,
                               TrainerWorkloadNotifier trainerWorkloadNotifier) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainerWorkloadNotifier = trainerWorkloadNotifier;
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

        trainerWorkloadNotifier.notifyWorkload(TrainerWorkloadRequestMapper.toRequest(training, ActionType.ADD));

        return training;
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

    @Override
    @Transactional
    public void deleteTraining(UUID trainingId) {
        LOG.debug("Deleting training: {}", trainingId);

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> {
                    LOG.error("Training not found: {}", trainingId);
                    return new NoSuchElementException("Training with id " + trainingId + " not found.");
                });

        Date now = new Date();
        if (!training.getTrainingDate().after(now)) {
            LOG.warn("Attempted to delete a non-future training: {}", trainingId);
            throw new TrainingDeletionNotAllowedException(
                    "Cannot delete a training that is not in the future: " + trainingId);
        }

        trainingRepository.delete(training);
        LOG.info("Deleted training: {}", trainingId);

        trainerWorkloadNotifier.notifyWorkload(TrainerWorkloadRequestMapper.toRequest(training, ActionType.DELETE));
    }
}