package gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.domain.User;
import gym.dto.request.TrainerRegistrationRequest;
import gym.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Base64;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade gymFacade;

    @Test
    void registerTrainer_withValidRequest_returns201AndCredentials() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");

        User user = new User("Fran", "Miche", "fran.miche", "generatedPass123", true);
        Trainer trainer = new Trainer(user, cardio);

        when(gymFacade.getTrainingTypeByName("Cardio")).thenReturn(cardio);
        when(gymFacade.createTrainer(eq("Fran"), eq("Miche"), eq(cardio)))
                .thenReturn(trainer);

        TrainerRegistrationRequest request =
                new TrainerRegistrationRequest("Fran", "Miche", "Cardio");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("fran.miche"))
                .andExpect(jsonPath("$.password").value("generatedPass123"));

        verify(gymFacade).createTrainer(eq("Fran"), eq("Miche"), eq(cardio));
    }

    @Test
    void registerTrainer_withMissingFirstName_returns400() throws Exception {
        String invalidJson = "{\"lastName\":\"Miche\",\"specialization\":\"Cardio\"}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withMissingLastName_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"Fran\",\"specialization\":\"Cardio\"}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withMissingSpecializationField_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"Fran\",\"lastName\":\"Miche\"}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withBlankSpecialization_returns400() throws Exception {
        String invalidJson =
                "{\"firstName\":\"Fran\",\"lastName\":\"Miche\",\"specialization\":\"   \"}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withNonStringSpecialization_returns400() throws Exception {
        String invalidJson =
                "{\"firstName\":\"Fran\",\"lastName\":\"Miche\",\"specialization\":123}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withNonexistentSpecialization_returns404() throws Exception {
        when(gymFacade.getTrainingTypeByName("Nonexistent"))
                .thenThrow(new NoSuchElementException("Training type 'Nonexistent' not found."));

        String json =
                "{\"firstName\":\"Fran\",\"lastName\":\"Miche\",\"specialization\":\"Nonexistent\"}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Test
    void updateTrainerStatus_withActiveTrue_returns200() throws Exception {
        String request = """
            {
                "username": "trainer.user",
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(gymFacade).updateTrainerActiveStatus(
                "admin",
                "adminPassword",
                "trainer.user",
                true
        );
    }

    @Test
    void updateTrainerStatus_withActiveFalse_returns200() throws Exception {
        String request = """
            {
                "username": "trainer.user",
                "active": false
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(gymFacade).updateTrainerActiveStatus(
                "admin",
                "adminPassword",
                "trainer.user",
                false
        );
    }

    @Test
    void updateTrainerStatus_withoutCredentials_returns401() throws Exception {
        String request = """
            {
                "username": "trainer.user",
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTrainerStatus_withMissingUsername_returns400() throws Exception {
        String request = """
            {
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTrainerStatus_withBlankUsername_returns400() throws Exception {
        String request = """
            {
                "username": "   ",
                "active": true
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTrainerStatus_withMissingActive_returns400() throws Exception {
        String request = """
            {
                "username": "trainer.user"
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTrainerStatus_withNullActive_returns400() throws Exception {
        String request = """
            {
                "username": "trainer.user",
                "active": null
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void updateTrainerStatus_withMalformedBoolean_returns400() throws Exception {
        String request = """
            {
                "username": "trainer.user",
                "active": fale
            }
            """;

        mockMvc.perform(patch("/api/trainers/status")
                        .header("Authorization", basicAuthHeader("admin", "adminPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));

        verifyNoInteractions(gymFacade);
    }
}