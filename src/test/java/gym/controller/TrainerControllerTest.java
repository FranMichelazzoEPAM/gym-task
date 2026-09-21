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

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        TrainingType yoga = new TrainingType("Yoga");

        User user = new User("Fran", "Miche", "fran.miche", "generatedPass123", true);
        Trainer trainer = new Trainer(user, List.of(cardio, yoga));

        when(gymFacade.getTrainingTypeByName("Cardio")).thenReturn(cardio);
        when(gymFacade.getTrainingTypeByName("Yoga")).thenReturn(yoga);
        when(gymFacade.createTrainer(eq("Fran"), eq("Miche"), eq(List.of(cardio, yoga))))
                .thenReturn(trainer);

        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "Fran", "Miche", List.of("Cardio", "Yoga"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("fran.miche"))
                .andExpect(jsonPath("$.password").value("generatedPass123"));

        verify(gymFacade).createTrainer(eq("Fran"), eq("Miche"), eq(List.of(cardio, yoga)));
    }

    @Test
    void registerTrainer_withMissingFirstName_returns400() throws Exception {
        String invalidJson = "{\"lastName\":\"Miche\",\"specializations\":[\"Cardio\"]}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withMissingLastName_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"Fran\",\"specializations\":[\"Cardio\"]}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withMissingSpecializationsField_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"Fran\",\"lastName\":\"Miche\"}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withEmptySpecializationsList_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"Fran\",\"lastName\":\"Miche\",\"specializations\":[]}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withBlankSpecializationInList_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"Fran\",\"lastName\":\"Miche\",\"specializations\":[\"Cardio\",\"   \"]}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_withNonexistentSpecialization_returns404() throws Exception {
        when(gymFacade.getTrainingTypeByName("Nonexistent"))
                .thenThrow(new NoSuchElementException("Training type 'Nonexistent' not found."));

        String json = "{\"firstName\":\"Fran\",\"lastName\":\"Miche\",\"specializations\":[\"Nonexistent\"]}";

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}