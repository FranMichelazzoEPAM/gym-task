package gym.service.impl;

import gym.dao.TrainerDao;
import gym.domain.Trainer;
import gym.service.PasswordGenerationService;
import gym.service.UsernameGenerationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TrainerServiceImplTest {

    @Test
    public void testCreateTrainerDelegatesAndGeneratesCredentials() {
        TrainerDao dao = Mockito.mock(TrainerDao.class);

        // Use simple stubs for concrete helper classes to avoid inline mocking
        UsernameGenerationService usernameSvc = new UsernameGenerationService() {
            @Override
            public String generateUsername(String firstName, String lastName) {
                return "jane.smith";
            }
        };
        PasswordGenerationService passwordSvc = new PasswordGenerationService() {
            @Override
            public String generateRandomPassword() {
                return "pw123";
            }
        };

        TrainerServiceImpl service = new TrainerServiceImpl();
        service.setTrainerDao(dao);
        service.setUsernameGenerationService(usernameSvc);
        service.setPasswordGenerationService(passwordSvc);

        Trainer t = service.createTrainer("Jane","Smith","yoga");

        Assertions.assertEquals("jane.smith", t.getUsername());
        Assertions.assertEquals("pw123", t.getPassword());
        verify(dao, times(1)).save(any(Trainer.class));
    }

    @Test
    public void testUpdateTrainerWhenNotFoundThrows() {
        TrainerDao dao = Mockito.mock(TrainerDao.class);
        when(dao.findById(any())).thenReturn(Optional.empty());

        TrainerServiceImpl service = new TrainerServiceImpl();
        service.setTrainerDao(dao);

        Trainer t = new Trainer("A","B","u","p",true, UUID.randomUUID(), "s");
        Assertions.assertThrows(java.util.NoSuchElementException.class, () -> service.updateTrainer(t));
    }

    @Test
    public void testGetTrainerWhenFoundReturns() {
        TrainerDao dao = Mockito.mock(TrainerDao.class);
        UUID id = UUID.randomUUID();
        Trainer expected = new Trainer("A","B","u","p",true, id, "s");
        when(dao.findById(id)).thenReturn(Optional.of(expected));

        TrainerServiceImpl service = new TrainerServiceImpl();
        service.setTrainerDao(dao);

        Trainer found = service.getTrainer(id);
        Assertions.assertEquals(expected, found);
    }
}
