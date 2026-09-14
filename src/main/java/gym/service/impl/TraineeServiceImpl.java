package gym.service.impl;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.User;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.service.PasswordGenerationService;
import gym.service.TraineeService;
import gym.service.UsernameGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerationService usernameGenerationService;
    private final PasswordGenerationService passwordGenerationService;

    @Autowired
    public TraineeServiceImpl(TraineeRepository traineeRepository,
                              TrainerRepository trainerRepository,
                              UsernameGenerationService usernameGenerationService,
                              PasswordGenerationService passwordGenerationService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.usernameGenerationService = usernameGenerationService;
        this.passwordGenerationService = passwordGenerationService;
        LOG.info("TraineeServiceImpl dependencies injected");
    }

    @Override
    @Transactional
    public Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address) {
        LOG.debug("Creating trainee: {} {}", firstName, lastName);

        String username = usernameGenerationService.generateUsername(firstName, lastName);
        String password = passwordGenerationService.generateRandomPassword();

        User user = new User(firstName, lastName, username, password, true);
        Trainee trainee = new Trainee(user, dateOfBirth, address);

        traineeRepository.save(trainee);
        LOG.info("Created trainee: {} (id={})", username, trainee.getTraineeId());
        return trainee;
    }

    @Override
    @Transactional
    public Trainee updateTrainee(Trainee trainee) {
        LOG.debug("Updating trainee: {}", trainee.getTraineeId());
        if (!traineeRepository.existsById(trainee.getTraineeId())) {
            LOG.error("Trainee not found for update: {}", trainee.getTraineeId());
            throw new NoSuchElementException("Trainee with id " + trainee.getTraineeId() + " not found.");
        }
        Trainee updated = traineeRepository.save(trainee);
        LOG.info("Updated trainee: {}", updated.getTraineeId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteTraineeByUsername(String username) {
        LOG.debug("Deleting trainee: {}", username);
        Trainee trainee = findTraineeOrThrow(username);
        traineeRepository.delete(trainee);
        LOG.info("Deleted trainee: {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee getTraineeByUsername(String username) {
        LOG.debug("Getting trainee: {}", username);
        return findTraineeOrThrow(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        LOG.debug("Getting all trainees");
        return traineeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        LOG.debug("Authenticating trainee: {}", username);
        boolean matches = traineeRepository.findByUser_Username(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
        if (!matches) {
            LOG.warn("Authentication failed for trainee: {}", username);
        }
        return matches;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        LOG.debug("Changing password for trainee: {}", username);
        Trainee trainee = findTraineeOrThrow(username);

        if (!trainee.getUser().getPassword().equals(oldPassword)) {
            LOG.error("Password change failed: old password mismatch for {}", username);
            throw new IllegalArgumentException("Old password does not match.");
        }

        trainee.getUser().setPassword(newPassword);
        traineeRepository.save(trainee);
        LOG.info("Password changed for trainee: {}", username);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(String username) {
        LOG.debug("Toggling active status for trainee: {}", username);
        Trainee trainee = findTraineeOrThrow(username);
        trainee.getUser().toggleActive();
        traineeRepository.save(trainee);
        LOG.info("Trainee {} active status is now {}", username, trainee.getUser().isActive());
    }

    @Override
    @Transactional
    public Trainee updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames) {
        LOG.debug("Updating trainers list for trainee: {}", traineeUsername);
        Trainee trainee = findTraineeOrThrow(traineeUsername);

        List<Trainer> resolvedTrainers = trainerUsernames.stream()
                .map(username -> trainerRepository.findByUser_Username(username)
                        .orElseThrow(() -> {
                            LOG.error("Trainer not found: {}", username);
                            return new NoSuchElementException("Trainer with username " + username + " not found.");
                        }))
                .collect(Collectors.toCollection(ArrayList::new));

        trainee.setTrainers(resolvedTrainers);
        Trainee updated = traineeRepository.save(trainee);
        LOG.info("Updated trainers list for trainee: {} ({} trainers)", traineeUsername, resolvedTrainers.size());
        return updated;
    }

    private Trainee findTraineeOrThrow(String username) {
        return traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> {
                    LOG.error("Trainee not found: {}", username);
                    return new NoSuchElementException("Trainee with username " + username + " not found.");
                });
    }
}