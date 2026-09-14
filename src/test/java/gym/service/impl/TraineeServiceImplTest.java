package gym.service.impl;

import gym.dao.TraineeRepository;
import gym.domain.Trainee;
import gym.service.PasswordGenerationService;
import gym.service.UsernameGenerationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TraineeServiceImplTest {

    @Test
    public void testCreateTraineeDelegatesAndGeneratesCredentials() {
        TraineeRepository dao = Mockito.mock(TraineeRepository.class);
        // Avoid mocking concrete helper classes that require JVM instrumentation — use small test stubs instead
        UsernameGenerationService usernameSvc = new UsernameGenerationService() {
            @Override
            public String generateUsername(String firstName, String lastName) {
                return "john.doe";
            }
        };
        PasswordGenerationService passwordSvc = new PasswordGenerationService() {
            @Override
            public String generateRandomPassword() {
                return "securepwd";
            }
        };

        TraineeServiceImpl service = new TraineeServiceImpl();
        service.setTraineeDao(dao);
        service.setUsernameGenerationService(usernameSvc);
        service.setPasswordGenerationService(passwordSvc);

        Trainee t = service.createTrainee("John","Doe", new Date(), "addr");

        Assertions.assertEquals("john.doe", t.getUsername());
        Assertions.assertEquals("securepwd", t.getPassword());
        verify(dao, times(1)).save(any(Trainee.class));
    }

    @Test
    public void testUpdateTraineeWhenNotFoundThrows() {
        TraineeRepository dao = Mockito.mock(TraineeRepository.class);
        when(dao.findById(any())).thenReturn(Optional.empty());

        TraineeServiceImpl service = new TraineeServiceImpl();
        service.setTraineeDao(dao);

        Trainee t = new Trainee("X","Y","u","p",true,new Date(),"a", UUID.randomUUID());
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> service.updateTrainee(t));
    }

    @Test
    public void testDeleteTraineeWhenFoundDeletes() {
        TraineeRepository dao = Mockito.mock(TraineeRepository.class);
        UUID id = UUID.randomUUID();
        when(dao.findById(id)).thenReturn(Optional.of(new Trainee("A","B","u","p",true,new Date(),"a", id)));

        TraineeServiceImpl service = new TraineeServiceImpl();
        service.setTraineeDao(dao);

        service.deleteTrainee(id);
        verify(dao, times(1)).delete(id);
    }
}
