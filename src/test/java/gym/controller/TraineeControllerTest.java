package gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.domain.User;
import gym.dto.request.TraineeRegistrationRequest;
import gym.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.never;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(TraineeController.class)
class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade gymFacade;

    @Test
    void registerTrainee_withValidRequest_returns201AndCredentials() throws Exception {
        User user = new User("John", "Doe", "john.doe", "generatedPass123", true);
        Trainee trainee = new Trainee(user, null, "123 Main St");

        when(gymFacade.createTrainee(eq("John"), eq("Doe"), any(), eq("123 Main St")))
                .thenReturn(trainee);

        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(1990, 5, 21), "123 Main St");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("generatedPass123"));
    }

    @Test
    void registerTrainee_withMissingFirstName_returns400() throws Exception {
        String invalidJson = "{\"lastName\":\"Doe\"}";

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainee_withBlankLastName_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"John\",\"lastName\":\"   \"}";

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Test
    void deleteTrainee_withValidCredentials_returns204() throws Exception {
        mockMvc.perform(delete("/api/trainees/john.doe")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword")))
                .andExpect(status().isNoContent());

        verify(gymFacade).deleteTrainee(
                "admin",
                "adminPassword",
                "john.doe"
        );
    }

    @Test
    void deleteTrainee_withoutCredentials_returns401() throws Exception {
        mockMvc.perform(delete("/api/trainees/john.doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTraineeStatus_withActiveTrue_returns200() throws Exception {
        String request = """
            {
                "username": "Maxi.Miliano",
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(gymFacade).updateTraineeActiveStatus(
                "admin",
                "adminPassword",
                "Maxi.Miliano",
                true
        );
    }

    @Test
    void updateTraineeStatus_withActiveFalse_returns200() throws Exception {
        String request = """
            {
                "username": "Maxi.Miliano",
                "active": false
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(gymFacade).updateTraineeActiveStatus(
                "admin",
                "adminPassword",
                "Maxi.Miliano",
                false
        );
    }

    @Test
    void updateTraineeStatus_withoutCredentials_returns401() throws Exception {
        String request = """
            {
                "username": "Maxi.Miliano",
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeStatus_withMissingUsername_returns400() throws Exception {
        String request = """
            {
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeStatus_withBlankUsername_returns400() throws Exception {
        String request = """
            {
                "username": "   ",
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeStatus_withMissingActive_returns400() throws Exception {
        String request = """
            {
                "username": "Maxi.Miliano"
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeStatus_withNullActive_returns400() throws Exception {
        String request = """
            {
                "username": "Maxi.Miliano",
                "active": null
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTraineeStatus_withMalformedBoolean_returns400() throws Exception {
        String request = """
            {
                "username": "Maxi.Miliano",
                "active": fale
            }
            """;

        mockMvc.perform(patch("/api/trainees/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));

        verifyNoInteractions(gymFacade);
    }

    @Test
    void getTrainee_withValidCredentials_returns200AndProfile() throws Exception {
        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "pass123", true);
        Trainee trainee = new Trainee(traineeUser, Date.valueOf("1995-06-15"), "123 Main St");

        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "trainerPass", true);
        Trainer trainer = new Trainer(trainerUser, cardio);
        trainee.setTrainers(List.of(trainer));

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(trainee);

        mockMvc.perform(get("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maxi"))
                .andExpect(jsonPath("$.lastName").value("Miliano"))
                .andExpect(jsonPath("$.dateOfBirth").value("1995-06-15"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.trainers", hasSize(1)))
                .andExpect(jsonPath("$.trainers[0].username").value("Fran.Miche1"))
                .andExpect(jsonPath("$.trainers[0].firstName").value("Fran"))
                .andExpect(jsonPath("$.trainers[0].lastName").value("Miche"))
                .andExpect(jsonPath("$.trainers[0].specialization").value("Cardio"));
    }

    @Test
    void getTrainee_withNoTrainers_returnsEmptyTrainersList() throws Exception {
        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "pass123", true);
        Trainee trainee = new Trainee(traineeUser, Date.valueOf("1995-06-15"), "123 Main St");
        // trainers left as default empty list

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(trainee);

        mockMvc.perform(get("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers", hasSize(0)));
    }

    @Test
    void getTrainee_withMissingAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/trainees/Maxi.Miliano"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrainee_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.getTrainee("Caller.User", "wrongPass", "Maxi.Miliano"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        mockMvc.perform(get("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrainee_withNonexistentUsername_returns404() throws Exception {
        when(gymFacade.getTrainee("Caller.User", "callerPass", "Ghost.User"))
                .thenThrow(new NoSuchElementException("Trainee with username Ghost.User not found."));

        mockMvc.perform(get("/api/trainees/Ghost.User")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isNotFound());
    }
}