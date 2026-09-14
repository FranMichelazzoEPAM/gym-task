package gym.facade.impl;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.facade.GymFacade;
import gym.security.RequiresAuthentication;
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
    public GymFacadeImpl(TraineeService traineeService,
                         TrainerService trainerService,
                         TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        LOG.info("GymFacadeImpl initialized");
    }

    // ===== Trainees =====

    @Override
    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        LOG.debug("Facade createTrainee {} {}", firstName, lastName);
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    @RequiresAuthentication
    @Override
    public Trainee updateTrainee(String callerUsername, String callerPassword, Trainee trainee) {
        LOG.debug("Facade updateTrainee {}", trainee.getTraineeId());
        return traineeService.updateTrainee(trainee);
    }

    @RequiresAuthentication
    @Override
    public void deleteTrainee(String callerUsername, String callerPassword, String username) {
        LOG.debug("Facade deleteTrainee {}", username);
        traineeService.deleteTraineeByUsername(username);
    }

    @RequiresAuthentication
    @Override
    public Trainee getTrainee(String callerUsername, String callerPassword, String username) {
        LOG.debug("Facade getTrainee {}", username);
        return traineeService.getTraineeByUsername(username);
    }

    @RequiresAuthentication
    @Override
    public List<Trainee> getAllTrainees(String callerUsername, String callerPassword) {
        LOG.debug("Facade getAllTrainees");
        return traineeService.getAllTrainees();
    }

    @Override
    public boolean authenticateTrainee(String username, String password) {
        LOG.debug("Facade authenticateTrainee {}", username);
        return traineeService.authenticate(username, password);
    }

    @RequiresAuthentication
    @Override
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        LOG.debug("Facade changeTraineePassword {}", username);
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    @RequiresAuthentication
    @Override
    public void toggleTraineeActiveStatus(String callerUsername, String callerPassword, String username) {
        LOG.debug("Facade toggleTraineeActiveStatus {}", username);
        traineeService.toggleActiveStatus(username);
    }

    @RequiresAuthentication
    @Override
    public Trainee updateTraineeTrainersList(String callerUsername, String callerPassword,
                                             String traineeUsername, List<String> trainerUsernames) {
        LOG.debug("Facade updateTraineeTrainersList {}", traineeUsername);
        return traineeService.updateTraineeTrainersList(traineeUsername, trainerUsernames);
    }

    // ===== Trainers =====

    @Override
    public Trainer createTrainer(String firstName, String lastName, List<TrainingType> specialization) {
        LOG.debug("Facade createTrainer {} {}", firstName, lastName);
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    @RequiresAuthentication
    @Override
    public Trainer updateTrainer(String callerUsername, String callerPassword, Trainer trainer) {
        LOG.debug("Facade updateTrainer {}", trainer.getTrainerId());
        return trainerService.updateTrainer(trainer);
    }

    @RequiresAuthentication
    @Override
    public Trainer getTrainer(String callerUsername, String callerPassword, String username) {
        LOG.debug("Facade getTrainer {}", username);
        return trainerService.getTrainerByUsername(username);
    }

    @RequiresAuthentication
    @Override
    public List<Trainer> getAllTrainers(String callerUsername, String callerPassword) {
        LOG.debug("Facade getAllTrainers");
        return trainerService.getAllTrainers();
    }

    @Override
    public boolean authenticateTrainer(String username, String password) {
        LOG.debug("Facade authenticateTrainer {}", username);
        return trainerService.authenticate(username, password);
    }

    @RequiresAuthentication
    @Override
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        LOG.debug("Facade changeTrainerPassword {}", username);
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    @RequiresAuthentication
    @Override
    public void toggleTrainerActiveStatus(String callerUsername, String callerPassword, String username) {
        LOG.debug("Facade toggleTrainerActiveStatus {}", username);
        trainerService.toggleActiveStatus(username);
    }

    @RequiresAuthentication
    @Override
    public List<Trainer> getTrainersNotAssignedToTrainee(String callerUsername, String callerPassword,
                                                         String traineeUsername) {
        LOG.debug("Facade getTrainersNotAssignedToTrainee {}", traineeUsername);
        return trainerService.getTrainersNotAssignedToTrainee(traineeUsername);
    }

    // ===== Trainings =====

    @RequiresAuthentication
    @Override
    public Training createTraining(String callerUsername, String callerPassword,
                                   String traineeUsername, String trainerUsername, String trainingName,
                                   TrainingType trainingType, Date trainingDate, int trainingDuration) {
        LOG.debug("Facade createTraining {} for trainee {}", trainingName, traineeUsername);
        return trainingService.createTraining(traineeUsername, trainerUsername, trainingName,
                trainingType, trainingDate, trainingDuration);
    }

    @RequiresAuthentication
    @Override
    public Training getTraining(String callerUsername, String callerPassword, UUID trainingId) {
        LOG.debug("Facade getTraining {}", trainingId);
        return trainingService.getTraining(trainingId);
    }

    @RequiresAuthentication
    @Override
    public List<Training> getAllTrainings(String callerUsername, String callerPassword) {
        LOG.debug("Facade getAllTrainings");
        return trainingService.getAllTrainings();
    }

    @RequiresAuthentication
    @Override
    public List<Training> getTraineeTrainings(String callerUsername, String callerPassword,
                                              String traineeUsername, Date fromDate, Date toDate,
                                              String trainerName, String trainingTypeName) {
        LOG.debug("Facade getTraineeTrainings {}", traineeUsername);
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @RequiresAuthentication
    @Override
    public List<Training> getTrainerTrainings(String callerUsername, String callerPassword,
                                              String trainerUsername, Date fromDate, Date toDate,
                                              String traineeName) {
        LOG.debug("Facade getTrainerTrainings {}", trainerUsername);
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }
}