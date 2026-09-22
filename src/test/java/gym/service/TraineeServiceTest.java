package gym.service;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.domain.User;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.service.impl.TraineeServiceImpl;
import gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class TraineeServiceTest {

    private TraineeRepository traineeRepository;

    private TrainerRepository trainerRepository;

    private UsernameGenerationService usernameGenerationService;

    private PasswordGenerationService passwordGenerationService;

    private TraineeServiceImpl traineeService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        traineeRepository = Mockito.mock(TraineeRepository.class);
        trainerRepository = Mockito.mock(TrainerRepository.class);
        usernameGenerationService = new UsernameGenerationService(Mockito.mock(gym.repository.UserRepository.class)) {
            @Override public String generateUsername(String firstName, String lastName) { return "tuser"; }
        };
        passwordGenerationService = new PasswordGenerationService() {
            @Override public String generateRandomPassword() { return "pwd"; }
        };
        traineeService = new TraineeServiceImpl(traineeRepository, trainerRepository, usernameGenerationService, passwordGenerationService);
    }

    @Test
    @DisplayName("createTrainee persists and returns trainee")
    void createTrainee() {
        when(traineeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Trainee t = traineeService.createTrainee("A","B", null, null);
        assertThat(t.getUser().getUsername()).isEqualTo("tuser");
    }

    @Test
    @DisplayName("authenticate returns false when not found")
    void authenticateFalse() {
        when(traineeRepository.findByUser_Username("x")).thenReturn(Optional.empty());
        boolean ok = traineeService.authenticate("x", "p");
        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("updateTraineeTrainersList resolves trainers and saves")
    void updateTraineeTrainersList() {
        User u = new User("F","L","user","pass", true);
        Trainee tr = new Trainee(u, null, null);
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(tr));

        Trainer t1 = new Trainer(new User("T","One","t1","p",true), new TrainingType("Cardio"));
        when(trainerRepository.findByUser_Username("t1")).thenReturn(Optional.of(t1));
        when(traineeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainee updated = traineeService.updateTraineeTrainersList("user", List.of("t1"));
        assertThat(updated.getTrainers()).hasSize(1);
        verify(trainerRepository).findByUser_Username("t1");
    }

    @Test
    @DisplayName("updateTraineeTrainersList fails when trainer missing")
    void updateTraineeTrainersListFails() {
        User u = new User("F","L","user","pass", true);
        Trainee tr = new Trainee(u, null, null);
        when(traineeRepository.findByUser_Username("user")).thenReturn(Optional.of(tr));
        when(trainerRepository.findByUser_Username("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> traineeService.updateTraineeTrainersList("user", List.of("missing")));
    }

    @Test
    @DisplayName("updateActiveStatus deactivates an active trainee")
    void updateActiveStatus_deactivatesActiveTrainee() {
        User user = new User("Maxi", "Miliano", "Maxi.Miliano", "password", true);
        Trainee trainee = new Trainee(user, null, null);

        when(traineeRepository.findByUser_Username("Maxi.Miliano"))
                .thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        traineeService.updateActiveStatus("Maxi.Miliano", false);

        assertThat(trainee.getUser().isActive()).isFalse();
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("updateActiveStatus activates an inactive trainee")
    void updateActiveStatus_activatesInactiveTrainee() {
        User user = new User("Maxi", "Miliano", "Maxi.Miliano", "password", false);
        Trainee trainee = new Trainee(user, null, null);

        when(traineeRepository.findByUser_Username("Maxi.Miliano"))
                .thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        traineeService.updateActiveStatus("Maxi.Miliano", true);

        assertThat(trainee.getUser().isActive()).isTrue();
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("updateActiveStatus does not toggle when the requested status is already set")
    void updateActiveStatus_keepsExistingStatus() {
        User user = new User("Maxi", "Miliano", "Maxi.Miliano", "password", true);
        Trainee trainee = new Trainee(user, null, null);

        when(traineeRepository.findByUser_Username("Maxi.Miliano"))
                .thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        traineeService.updateActiveStatus("Maxi.Miliano", true);

        assertThat(trainee.getUser().isActive()).isTrue();
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("updateActiveStatus throws when trainee does not exist")
    void updateActiveStatus_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUser_Username("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> traineeService.updateActiveStatus("missing", true)
        );
    }
}
