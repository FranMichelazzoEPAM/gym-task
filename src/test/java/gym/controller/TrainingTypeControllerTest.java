package gym.controller;

import gym.config.SecurityConfig;
import gym.domain.TrainingType;
import gym.facade.GymFacade;
import gym.security.service.CustomUserDetailsService;
import gym.testutil.MetricsTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingTypeController.class)
@Import({SecurityConfig.class, MetricsTestConfig.class})
class TrainingTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void getAllTrainingTypes_returns200AndList() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        TrainingType yoga = new TrainingType("Yoga");

        when(gymFacade.getAllTrainingTypes()).thenReturn(List.of(cardio, yoga));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Cardio"))
                .andExpect(jsonPath("$[1].name").value("Yoga"));
    }

    @Test
    void getAllTrainingTypes_whenNoneExist_returnsEmptyList() throws Exception {
        when(gymFacade.getAllTrainingTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}