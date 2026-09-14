package gym.security;

import gym.service.TraineeService;
import gym.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuthenticationAspectTest.TestConfig.class)
class AuthenticationAspectTest {

    @Autowired
    private DummyService dummyService;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Test
    void shouldAllowCallWhenTraineeAuthenticationSucceeds() {
        when(traineeService.authenticate("john", "pass123")).thenReturn(true);
        when(trainerService.authenticate("john", "pass123")).thenReturn(false);

        String result = dummyService.protectedMethod("john", "pass123");

        assertEquals("success", result);
    }

    @Test
    void shouldAllowCallWhenTrainerAuthenticationSucceeds() {
        when(traineeService.authenticate("mary", "secret")).thenReturn(false);
        when(trainerService.authenticate("mary", "secret")).thenReturn(true);

        String result = dummyService.protectedMethod("mary", "secret");

        assertEquals("success", result);
    }

    @Test
    void shouldBlockCallWhenAuthenticationFails() {
        when(traineeService.authenticate("hacker", "wrong")).thenReturn(false);
        when(trainerService.authenticate("hacker", "wrong")).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class,
                () -> dummyService.protectedMethod("hacker", "wrong"));

        assertTrue(ex.getMessage().contains("hacker"));
    }

    // ---- Minimal test context ----

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        TraineeService traineeService() {
            return mock(TraineeService.class);
        }

        @Bean
        TrainerService trainerService() {
            return mock(TrainerService.class);
        }

        @Bean
        AuthenticationAspect authenticationAspect(TraineeService traineeService, TrainerService trainerService) {
            return new AuthenticationAspect(traineeService, trainerService);
        }

        @Bean
        DummyService dummyService() {
            return new DummyService();
        }
    }

    // A throwaway target bean, just to exercise the aspect without needing the real facade
    static class DummyService {
        @RequiresAuthentication
        public String protectedMethod(String username, String password) {
            return "success";
        }
    }
}