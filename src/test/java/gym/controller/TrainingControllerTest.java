package gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.domain.User;
import gym.dto.request.TrainingRegistrationRequest;
import gym.facade.GymFacade;
import gym.security.jwt.JwtUtil;
import gym.security.service.CustomUserDetailsService;
import gym.testutil.MetricsTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.NoSuchElementException;

import static gym.testutil.AuthTestUtils.basicAuthHeader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MetricsTestConfig.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void addTraining_withValidRequest_returns200() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "pass123", true);
        Trainer trainer = new Trainer(trainerUser, cardio);

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(trainer);

        LocalDate date = LocalDate.of(2026, 1, 15);
        Date expectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        TrainingRegistrationRequest request = new TrainingRegistrationRequest(
                "Maxi.Miliano", "Fran.Miche1", "Morning Cardio", date, 60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).createTraining(
                eq("Caller.User"), eq("callerPass"),
                eq("Maxi.Miliano"), eq("Fran.Miche1"),
                eq("Morning Cardio"), eq(cardio),
                eq(expectedDate), eq(60));
    }

    @Test
    void addTraining_withMissingTraineeUsername_returns400() throws Exception {
        String invalidJson = """
            {
              "trainerUsername": "Fran.Miche1",
              "name": "Morning Cardio",
              "date": "2026-01-15",
              "duration": 60
            }
            """;

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_withMissingTrainerUsername_returns400() throws Exception {
        String invalidJson = """
            {
              "traineeUsername": "Maxi.Miliano",
              "name": "Morning Cardio",
              "date": "2026-01-15",
              "duration": 60
            }
            """;

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_withMissingName_returns400() throws Exception {
        String invalidJson = """
            {
              "traineeUsername": "Maxi.Miliano",
              "trainerUsername": "Fran.Miche1",
              "date": "2026-01-15",
              "duration": 60
            }
            """;

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_withMissingDate_returns400() throws Exception {
        String invalidJson = """
            {
              "traineeUsername": "Maxi.Miliano",
              "trainerUsername": "Fran.Miche1",
              "name": "Morning Cardio",
              "duration": 60
            }
            """;

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_withMissingDuration_returns400() throws Exception {
        String invalidJson = """
            {
              "traineeUsername": "Maxi.Miliano",
              "trainerUsername": "Fran.Miche1",
              "name": "Morning Cardio",
              "date": "2026-01-15"
            }
            """;

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_withNonPositiveDuration_returns400() throws Exception {
        String invalidJson = """
            {
              "traineeUsername": "Maxi.Miliano",
              "trainerUsername": "Fran.Miche1",
              "name": "Morning Cardio",
              "date": "2026-01-15",
              "duration": 0
            }
            """;

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_withMissingAuthorizationHeader_returns401() throws Exception {
        TrainingRegistrationRequest request = new TrainingRegistrationRequest(
                "Maxi.Miliano", "Fran.Miche1", "Morning Cardio", LocalDate.of(2026, 1, 15), 60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.getTrainer("Caller.User", "wrongPass", "Fran.Miche1"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        TrainingRegistrationRequest request = new TrainingRegistrationRequest(
                "Maxi.Miliano", "Fran.Miche1", "Morning Cardio", LocalDate.of(2026, 1, 15), 60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_withNonexistentTrainer_returns404() throws Exception {
        when(gymFacade.getTrainer("Caller.User", "callerPass", "Ghost.Trainer"))
                .thenThrow(new NoSuchElementException("Trainer with username Ghost.Trainer not found."));

        TrainingRegistrationRequest request = new TrainingRegistrationRequest(
                "Maxi.Miliano", "Ghost.Trainer", "Morning Cardio", LocalDate.of(2026, 1, 15), 60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTraining_withNonexistentTrainee_returns404() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "pass123", true);
        Trainer trainer = new Trainer(trainerUser, cardio);

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(trainer);

        when(gymFacade.createTraining(
                eq("Caller.User"), eq("callerPass"),
                eq("Ghost.Trainee"), eq("Fran.Miche1"),
                eq("Morning Cardio"), eq(cardio),
                any(Date.class), eq(60)))
                .thenThrow(new NoSuchElementException("Trainee with username Ghost.Trainee not found."));

        TrainingRegistrationRequest request = new TrainingRegistrationRequest(
                "Ghost.Trainee", "Fran.Miche1", "Morning Cardio", LocalDate.of(2026, 1, 15), 60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}