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
}