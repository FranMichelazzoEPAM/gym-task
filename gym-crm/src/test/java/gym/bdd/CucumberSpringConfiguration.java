package gym.bdd;

import gym.GymAppInitializer;
import gym.client.TrainerWorkloadNotifier;
import gym.client.TrainerWorkloadQueryClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = GymAppInitializer.class)
@AutoConfigureMockMvc
@ActiveProfiles("component-test")
public class CucumberSpringConfiguration {

    @MockBean
    private TrainerWorkloadNotifier trainerWorkloadNotifier;

    @MockBean
    private TrainerWorkloadQueryClient trainerWorkloadQueryClient;
}
