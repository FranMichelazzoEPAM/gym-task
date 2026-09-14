package gym.service.impl;

import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.domain.User;
import gym.repository.TrainerRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.PasswordGenerationService;
import gym.service.TrainerService;
import gym.service.UsernameGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameGenerationService usernameGenerationService;
    private final PasswordGenerationService passwordGenerationService;

    @Autowired
    public TrainerServiceImpl(TrainerRepository trainerRepository,
                              TrainingTypeRepository trainingTypeRepository,
                              UsernameGenerationService usernameGenerationService,
                              PasswordGenerationService passwordGenerationService) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.usernameGenerationService = usernameGenerationService;
        this.passwordGenerationService = passwordGenerationService;
        LOG.info("TrainerServiceImpl dependencies injected");
    }

    @Override
    @Transactional
    public Trainer createTrainer(String firstName, String lastName, List<TrainingType> specialization) {
        LOG.debug("Creating trainer: {} {}", firstName, lastName);

        if (specialization == null || specialization.isEmpty()) {
            LOG.error("Trainer creation failed: no specialization provided for {} {}", firstName, lastName);
            throw new IllegalArgumentException("Trainer must have at least one specialization.");
        }

        List<TrainingType> managedTypes = specialization.stream()
                .map(t -> trainingTypeRepository.findById(t.getId())
                        .orElseThrow(() -> {
                            LOG.error("Unknown training type id: {}", t.getId());
                            return new NoSuchElementException("Training type not found: " + t.getId());
                        }))
                .toList();

        String username = usernameGenerationService.generateUsername(firstName, lastName);
        String password = passwordGenerationService.generateRandomPassword();
        User user = new User(firstName, lastName, username, password, true);

        Trainer trainer = new Trainer(user, managedTypes);
        trainerRepository.save(trainer);
        LOG.info("Created trainer: {} (id={})", username, trainer.getTrainerId());

        return trainer;
    }

    @Override
    @Transactional
    public Trainer updateTrainer(Trainer trainer) {
        LOG.debug("Updating trainer: {}", trainer.getTrainerId());
        if (!trainerRepository.existsById(trainer.getTrainerId())) {
            LOG.error("Trainer not found for update: {}", trainer.getTrainerId());
            throw new NoSuchElementException("Trainer with id " + trainer.getTrainerId() + " not found.");
        }
        Trainer updated = trainerRepository.save(trainer);
        LOG.info("Updated trainer: {}", updated.getTrainerId());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(String username) {
        LOG.debug("Getting trainer: {}", username);
        return findTrainerOrThrow(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        LOG.debug("Getting all trainers");
        return trainerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        LOG.debug("Authenticating trainer: {}", username);
        boolean matches = trainerRepository.findByUser_Username(username)
                .map(t -> t.getUser().getPassword().equals(password))
                .orElse(false);
        if (!matches) {
            LOG.warn("Authentication failed for trainer: {}", username);
        }
        return matches;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        LOG.debug("Changing password for trainer: {}", username);
        Trainer trainer = findTrainerOrThrow(username);

        if (!trainer.getUser().getPassword().equals(oldPassword)) {
            LOG.error("Password change failed: old password mismatch for {}", username);
            throw new IllegalArgumentException("Old password does not match.");
        }

        trainer.getUser().setPassword(newPassword);
        trainerRepository.save(trainer);
        LOG.info("Password changed for trainer: {}", username);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(String username) {
        LOG.debug("Toggling active status for trainer: {}", username);
        Trainer trainer = findTrainerOrThrow(username);
        trainer.getUser().toggleActive();
        trainerRepository.save(trainer);
        LOG.info("Trainer {} active status is now {}", username, trainer.getUser().isActive());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        LOG.debug("Getting trainers not assigned to trainee: {}", traineeUsername);
        return trainerRepository.findUnassignedTrainersForTrainee(traineeUsername);
    }

    private Trainer findTrainerOrThrow(String username) {
        return trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> {
                    LOG.error("Trainer not found: {}", username);
                    return new NoSuchElementException("Trainer with username " + username + " not found.");
                });
    }
}