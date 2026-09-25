package gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.domain.Trainee;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;

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
    void getTrainer_withValidCredentials_returns200AndProfile() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "trainerPass", true);
        Trainer trainer = new Trainer(trainerUser, cardio);

        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "pass123", true);
        Trainee trainee = new Trainee(traineeUser, null, null);
        trainer.getTrainees().add(trainee);

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(trainer);

        mockMvc.perform(get("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Fran"))
                .andExpect(jsonPath("$.lastName").value("Miche"))
                .andExpect(jsonPath("$.specialization").value("Cardio"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.trainees", hasSize(1)))
                .andExpect(jsonPath("$.trainees[0].username").value("Maxi.Miliano"))
                .andExpect(jsonPath("$.trainees[0].firstName").value("Maxi"))
                .andExpect(jsonPath("$.trainees[0].lastName").value("Miliano"));
    }

    @Test
    void getTrainer_withNoTrainees_returnsEmptyTraineesList() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "trainerPass", true);
        Trainer trainer = new Trainer(trainerUser, cardio);

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(trainer);

        mockMvc.perform(get("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainees", hasSize(0)));
    }

    @Test
    void getTrainer_withMissingAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/trainers/Fran.Miche1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrainer_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.getTrainer("Caller.User", "wrongPass", "Fran.Miche1"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        mockMvc.perform(get("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTrainer_withNonexistentUsername_returns404() throws Exception {
        when(gymFacade.getTrainer("Caller.User", "callerPass", "Ghost.User"))
                .thenThrow(new NoSuchElementException("Trainer with username Ghost.User not found."));

        mockMvc.perform(get("/api/trainers/Ghost.User")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotAssignedOnTraineeActiveTrainers_withValidCredentials_returnsTrainerSummaries()
            throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        TrainingType strength = new TrainingType("Strength");

        Trainer cardioTrainer = new Trainer(
                new User("Fran", "Miche", "Fran.Miche1", "trainerPass", true),
                cardio);
        Trainer strengthTrainer = new Trainer(
                new User("Ana", "Silva", "Ana.Silva", "trainerPass", true),
                strength);

        when(gymFacade.getTrainersNotAssignedToTrainee(
                "Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(List.of(cardioTrainer, strengthTrainer));

        mockMvc.perform(get("/api/trainers/not-assigned-on-trainee/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("Fran.Miche1"))
                .andExpect(jsonPath("$[0].firstName").value("Fran"))
                .andExpect(jsonPath("$[0].lastName").value("Miche"))
                .andExpect(jsonPath("$[0].specialization").value("Cardio"))
                .andExpect(jsonPath("$[1].username").value("Ana.Silva"))
                .andExpect(jsonPath("$[1].firstName").value("Ana"))
                .andExpect(jsonPath("$[1].lastName").value("Silva"))
                .andExpect(jsonPath("$[1].specialization").value("Strength"));

        verify(gymFacade).getTrainersNotAssignedToTrainee(
                "Caller.User", "callerPass", "Maxi.Miliano");
    }

    @Test
    void getNotAssignedOnTraineeActiveTrainers_whenNoTrainersAreAvailable_returnsEmptyList()
            throws Exception {
        when(gymFacade.getTrainersNotAssignedToTrainee(
                "Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/not-assigned-on-trainee/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getNotAssignedOnTraineeActiveTrainers_withMissingAuthorizationHeader_returns401()
            throws Exception {
        mockMvc.perform(get("/api/trainers/not-assigned-on-trainee/Maxi.Miliano"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void getNotAssignedOnTraineeActiveTrainers_withInvalidCredentials_returns401()
            throws Exception {
        when(gymFacade.getTrainersNotAssignedToTrainee(
                "Caller.User", "wrongPass", "Maxi.Miliano"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        mockMvc.perform(get("/api/trainers/not-assigned-on-trainee/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getNotAssignedOnTraineeActiveTrainers_withNonexistentTrainee_returns404()
            throws Exception {
        when(gymFacade.getTrainersNotAssignedToTrainee(
                "Caller.User", "callerPass", "Ghost.User"))
                .thenThrow(new NoSuchElementException(
                        "Trainee with username Ghost.User not found."));

        mockMvc.perform(get("/api/trainers/not-assigned-on-trainee/Ghost.User")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isNotFound());
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

    @Test
    void updateTrainer_withValidRequest_returns200AndUpdatedProfile() throws Exception {
        TrainingType oldSpecialization = new TrainingType("Cardio");
        User existingUser = new User("OldFirst", "OldLast", "Fran.Miche1", "pass123", true);
        Trainer existingTrainer = new Trainer(existingUser, oldSpecialization);

        TrainingType newSpecialization = new TrainingType("Yoga");
        User updatedUser = new User("NewFirst", "NewLast", "Fran.Miche1", "pass123", true);
        Trainer updatedTrainer = new Trainer(updatedUser, newSpecialization);

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(existingTrainer);
        when(gymFacade.getTrainingTypeByName("Yoga"))
                .thenReturn(newSpecialization);
        when(gymFacade.updateTrainer(eq("Caller.User"), eq("callerPass"), any(Trainer.class)))
                .thenReturn(updatedTrainer);

        String requestBody = """
        {
          "firstName": "NewFirst",
          "lastName": "NewLast",
          "specialization": "Yoga",
          "active": true
        }
        """;

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Fran.Miche1"))
                .andExpect(jsonPath("$.firstName").value("NewFirst"))
                .andExpect(jsonPath("$.lastName").value("NewLast"))
                .andExpect(jsonPath("$.specialization").value("Yoga"))
                .andExpect(jsonPath("$.active").value(true));

        verify(gymFacade).updateTrainerActiveStatus("Caller.User", "callerPass", "Fran.Miche1", true);
    }

    @Test
    void updateTrainer_reconcilesActiveStatusInResponse_evenIfStaleInReturnedObject() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");

        User existingUser = new User("First", "Last", "Fran.Miche1", "pass123", false);
        Trainer existingTrainer = new Trainer(existingUser, cardio);

        User staleUser = new User("First", "Last", "Fran.Miche1", "pass123", false);
        Trainer staleUpdatedTrainer = new Trainer(staleUser, cardio);

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(existingTrainer);
        when(gymFacade.getTrainingTypeByName("Cardio"))
                .thenReturn(cardio);
        when(gymFacade.updateTrainer(eq("Caller.User"), eq("callerPass"), any(Trainer.class)))
                .thenReturn(staleUpdatedTrainer);

        String requestBody = """
        {
          "firstName": "First",
          "lastName": "Last",
          "specialization": "Cardio",
          "active": true
        }
        """;

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(gymFacade).updateTrainerActiveStatus("Caller.User", "callerPass", "Fran.Miche1", true);
    }

    @Test
    void updateTrainer_withMissingFirstName_returns400() throws Exception {
        String invalidJson = "{\"lastName\":\"Last\",\"specialization\":\"Cardio\",\"active\":true}";

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainer_withMissingSpecialization_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"active\":true}";

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainer_withMissingActive_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"specialization\":\"Cardio\"}";

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainer_withNonexistentSpecialization_returns404() throws Exception {
        User existingUser = new User("First", "Last", "Fran.Miche1", "pass123", true);
        Trainer existingTrainer = new Trainer(existingUser, new TrainingType("Cardio"));

        when(gymFacade.getTrainer("Caller.User", "callerPass", "Fran.Miche1"))
                .thenReturn(existingTrainer);
        when(gymFacade.getTrainingTypeByName("Nonexistent"))
                .thenThrow(new NoSuchElementException("Training type 'Nonexistent' not found."));

        String requestBody = """
        {
          "firstName": "First",
          "lastName": "Last",
          "specialization": "Nonexistent",
          "active": true
        }
        """;

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainer_withMissingAuthorizationHeader_returns401() throws Exception {
        String body = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"specialization\":\"Cardio\",\"active\":true}";

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTrainer_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.getTrainer("Caller.User", "wrongPass", "Fran.Miche1"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        String body = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"specialization\":\"Cardio\",\"active\":true}";

        mockMvc.perform(put("/api/trainers/Fran.Miche1")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTrainer_withNonexistentUsername_returns404() throws Exception {
        when(gymFacade.getTrainer("Caller.User", "callerPass", "Ghost.Trainer"))
                .thenThrow(new NoSuchElementException("Trainer with username Ghost.Trainer not found."));

        String body = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"specialization\":\"Cardio\",\"active\":true}";

        mockMvc.perform(put("/api/trainers/Ghost.Trainer")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}