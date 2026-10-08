package gym.facade.impl;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.facade.GymFacade;
import gym.service.*;
import gym.service.result.TraineeRegistrationResult;
import gym.service.result.TrainerRegistrationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GymFacadeImpl implements GymFacade {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GymFacadeImpl.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final UserService userService;

    @Autowired
    public GymFacadeImpl(TraineeService traineeService,
                         TrainerService trainerService,
                         TrainingService trainingService,
                         TrainingTypeService trainingTypeService,
                         UserService userService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.userService = userService;
        LOG.info("GymFacadeImpl initialized");
    }

    // ===== Users =====
    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        LOG.debug("Facade changePassword for {}", username);
        userService.changePassword(username, oldPassword, newPassword);
    }

    // ===== Trainees =====

    @Override
    public TraineeRegistrationResult createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        LOG.debug("Facade createTrainee {} {}", firstName, lastName);
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        LOG.debug("Facade updateTrainee {}", trainee.getTraineeId());
        return traineeService.updateTrainee(trainee);
    }

    @Override
    public void deleteTrainee(String username) {
        LOG.debug("Facade deleteTrainee {}", username);
        traineeService.deleteTraineeByUsername(username);
    }

    @Override
    public Trainee getTrainee(String username) {
        LOG.debug("Facade getTrainee {}", username);
        return traineeService.getTraineeByUsername(username);
    }

    @Override
    public void updateTraineeActiveStatus(String username, boolean active) {
        LOG.debug("Facade updateTraineeActiveStatus {}", username);
        traineeService.updateActiveStatus(username, active);
    }

    @Override
    public Trainee updateTraineeTrainersList(
            String traineeUsername, List<String> trainerUsernames) {
        LOG.debug("Facade updateTraineeTrainersList {}", traineeUsername);
        return traineeService.updateTraineeTrainersList(traineeUsername, trainerUsernames);
    }

    // ===== Trainers =====

    @Override
    public TrainerRegistrationResult createTrainer(String firstName, String lastName, TrainingType specialization) {
        LOG.debug("Facade createTrainer {} {}", firstName, lastName);
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        LOG.debug("Facade updateTrainer {}", trainer.getTrainerId());
        return trainerService.updateTrainer(trainer);
    }

    @Override
    public Trainer getTrainer(String username) {
        LOG.debug("Facade getTrainer {}", username);
        return trainerService.getTrainerByUsername(username);
    }

    @Override
    public void updateTrainerActiveStatus(String username, boolean active) {
        LOG.debug("Facade updateTrainerActiveStatus {}", username);
        trainerService.updateActiveStatus(username, active);
    }

    @Override
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        LOG.debug("Facade getTrainersNotAssignedToTrainee {}", traineeUsername);
        return trainerService.getTrainersNotAssignedToTrainee(traineeUsername);
    }

    // ===== Trainings =====

    @Override
    public Training createTraining(String traineeUsername, String trainerUsername, String trainingName,
                                   TrainingType trainingType, Date trainingDate, int trainingDuration) {
        LOG.debug("Facade createTraining {} for trainee {}", trainingName, traineeUsername);
        return trainingService.createTraining(traineeUsername, trainerUsername, trainingName,
                trainingType, trainingDate, trainingDuration);
    }

    @Override
    public List<Training> getTraineeTrainings(String traineeUsername, Date fromDate, Date toDate,
                                              String trainerName, String trainingTypeName) {
        LOG.debug("Facade getTraineeTrainings {}", traineeUsername);
        return trainingService.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Override
    public List<Training> getTrainerTrainings(String trainerUsername, Date fromDate, Date toDate,
                                              String traineeName) {
        LOG.debug("Facade getTrainerTrainings {}", trainerUsername);
        return trainingService.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName);
    }

    // ===== TrainingTypes =====
    @Override
    public List<TrainingType> getAllTrainingTypes() {
        LOG.debug("Facade getAllTrainingTypes");
        return trainingTypeService.getAllTrainingTypes();
    }

    @Override
    public TrainingType getTrainingTypeByName(String name) {
        LOG.debug("Facade getTrainingTypeByName {}", name);
        return trainingTypeService.getTrainingTypeByName(name);
    }
}