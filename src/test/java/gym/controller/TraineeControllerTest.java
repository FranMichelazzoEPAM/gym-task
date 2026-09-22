package gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.domain.Trainee;
import gym.domain.User;
import gym.dto.request.TraineeRegistrationRequest;
import gym.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

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
}