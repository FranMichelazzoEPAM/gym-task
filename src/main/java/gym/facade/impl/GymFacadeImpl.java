package gym.facade.impl;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.facade.GymFacade;
import gym.service.TraineeService;
import gym.service.TrainerService;
import gym.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class GymFacadeImpl implements GymFacade {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GymFacadeImpl.class);
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacadeImpl(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService) {

        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        LOG.info("GymFacadeImpl initialized");
    }

    // Trainees
    @Override
    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        LOG.debug("Facade createTrainee {} {}", firstName, lastName);
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        LOG.debug("Facade updateTrainee {}", trainee.getUserId());
        return traineeService.updateTrainee(trainee);
    }

    @Override
    public void deleteTrainee(UUID userId) {
        LOG.debug("Facade deleteTrainee {}", userId);
        traineeService.deleteTrainee(userId);
    }

    @Override
    public Trainee getTrainee(UUID userId) {
        LOG.debug("Facade getTrainee {}", userId);
        return traineeService.getTrainee(userId);
    }

    @Override
    public List<Trainee> getAllTrainees() {
        LOG.debug("Facade getAllTrainees");
        return traineeService.getAllTrainees();
    }

    //Trainers
    @Override
    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        LOG.debug("Facade createTrainer {} {}", firstName, lastName);
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        LOG.debug("Facade updateTrainer {}", trainer.getUserId());
        return trainerService.updateTrainer(trainer);
    }

    @Override
    public Trainer getTrainer(UUID userId) {
        LOG.debug("Facade getTrainer {}", userId);
        return trainerService.getTrainer(userId);
    }

    @Override
    public List<Trainer> getAllTrainers() {
        LOG.debug("Facade getAllTrainers");
        return trainerService.getAllTrainers();
    }

    // Trainings
    @Override
    public Training createTraining(UUID traineeId, UUID trainerId, String trainingName, TrainingType trainingType, Date trainingDate, java.time.Duration trainingDuration) {
        LOG.debug("Facade createTraining {} for trainee {}", trainingName, traineeId);
        return trainingService.createTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration);
    }

    @Override
    public Training getTraining(UUID trainingId) {
        LOG.debug("Facade getTraining {}", trainingId);
        return trainingService.getTraining(trainingId);
    }

    @Override
    public List<Training> getAllTrainings() {
        LOG.debug("Facade getAllTrainings");
        return trainingService.getAllTrainings();
    }
}