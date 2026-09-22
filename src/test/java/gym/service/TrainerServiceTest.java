package gym.service;

import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.domain.User;
import gym.repository.TrainerRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.impl.TrainerServiceImpl;
import gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerRepository trainerRepository;

    private TrainingTypeRepository trainingTypeRepository;

    private UsernameGenerationService usernameGenerationService;

    private PasswordGenerationService passwordGenerationService;

    private TrainerServiceImpl trainerService;

    private UserServiceImpl userService;

    private TrainingType managedType;

    @BeforeEach
    void setUp() {
        trainerRepository = Mockito.mock(TrainerRepository.class);
        trainingTypeRepository = Mockito.mock(TrainingTypeRepository.class);
        // Provide simple stubs for username/password generators
        usernameGenerationService = new UsernameGenerationService(Mockito.mock(gym.repository.UserRepository.class)) {
            @Override
            public String generateUsername(String firstName, String lastName) { return firstName.toLowerCase().charAt(0) + lastName.toLowerCase(); }
        };
        passwordGenerationService = new PasswordGenerationService() {
            @Override
            public String generateRandomPassword() { return "pwd"; }
        };
        trainerService = new TrainerServiceImpl(trainerRepository, trainingTypeRepository, usernameGenerationService, passwordGenerationService);

        managedType = new TrainingType("Cardio");
    }

    @Test
    @DisplayName("createTrainer succeeds with valid specialization")
    void createTrainerSuccess() {
        when(trainingTypeRepository.findById(any())).thenReturn(Optional.of(managedType));

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = trainerService.createTrainer("John", "Doe", List.of(new TrainingType("x")));

        assertThat(result.getUser().getUsername()).isEqualTo("jdoe");
        assertThat(result.getUser().getPassword()).isEqualTo("pwd");
        verify(trainerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createTrainer fails when specialization empty")
    void createTrainerFailsOnEmptySpecialization() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.createTrainer("A","B", List.of()));
    }

    @Test
    @DisplayName("authenticate returns true when password matches")
    void authenticateTrue() {
        User u = new User("F","L","user","secret", true);
        Trainer t = new Trainer(u, List.of());
        when(trainerRepository.findByUser_Username("user")).thenReturn(Optional.of(t));

        boolean ok = trainerService.authenticate("user", "secret");
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("updateActiveStatus deactivates an active trainer")
    void updateActiveStatus() {
        User user = new User("F", "L", "user", "p", true);
        Trainer trainer = new Trainer(user, List.of());

        when(trainerRepository.findByUser_Username("user"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.updateActiveStatus("user", false);

        assertThat(trainer.getUser().isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("updateActiveStatus activates an inactive trainer")
    void updateActiveStatus_activatesTrainer() {
        User user = new User("F", "L", "user", "p", false);
        Trainer trainer = new Trainer(user, List.of());

        when(trainerRepository.findByUser_Username("user"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.updateActiveStatus("user", true);

        assertThat(trainer.getUser().isActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("getTrainersNotAssignedToTrainee delegates to repository")
    void getTrainersNotAssigned() {
        when(trainerRepository.findUnassignedTrainersForTrainee("tuser")).thenReturn(List.of());
        var list = trainerService.getTrainersNotAssignedToTrainee("tuser");
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("updateActiveStatus deactivates an active trainer")
    void updateActiveStatus_deactivatesActiveTrainer() {
        User user = new User("John", "Trainer", "trainer.user", "password", true);
        Trainer trainer = new Trainer(user, List.of());

        when(trainerRepository.findByUser_Username("trainer.user"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.updateActiveStatus("trainer.user", false);

        assertThat(trainer.getUser().isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("updateActiveStatus activates an inactive trainer")
    void updateActiveStatus_activatesInactiveTrainer() {
        User user = new User("John", "Trainer", "trainer.user", "password", false);
        Trainer trainer = new Trainer(user, List.of());

        when(trainerRepository.findByUser_Username("trainer.user"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.updateActiveStatus("trainer.user", true);

        assertThat(trainer.getUser().isActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("updateActiveStatus keeps the current status when it already matches")
    void updateActiveStatus_keepsExistingStatus() {
        User user = new User("John", "Trainer", "trainer.user", "password", true);
        Trainer trainer = new Trainer(user, List.of());

        when(trainerRepository.findByUser_Username("trainer.user"))
                .thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.updateActiveStatus("trainer.user", true);

        assertThat(trainer.getUser().isActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("updateActiveStatus throws when trainer does not exist")
    void updateActiveStatus_throwsWhenTrainerDoesNotExist() {
        when(trainerRepository.findByUser_Username("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> trainerService.updateActiveStatus("missing", true)
        );
    }
}
