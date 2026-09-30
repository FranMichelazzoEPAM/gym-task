package gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.domain.*;
import gym.dto.request.TraineeRegistrationRequest;
import gym.facade.GymFacade;
import gym.testutil.MetricsTestConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
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
@Import(MetricsTestConfig.class)
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

    @Test
    void updateTrainee_withValidRequest_returns200AndUpdatedProfile() throws Exception {
        User existingUser = new User("OldFirst", "OldLast", "Maxi.Miliano", "pass123", true);
        Trainee existingTrainee = new Trainee(existingUser, Date.valueOf("1990-01-01"), "Old Address");

        User updatedUser = new User("NewFirst", "NewLast", "Maxi.Miliano", "pass123", true);
        Trainee updatedTrainee = new Trainee(updatedUser, Date.valueOf("1992-02-02"), "New Address");

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(existingTrainee);
        when(gymFacade.updateTrainee(eq("Caller.User"), eq("callerPass"), any(Trainee.class)))
                .thenReturn(updatedTrainee);

        String requestBody = """
        {
          "firstName": "NewFirst",
          "lastName": "NewLast",
          "dateOfBirth": "1992-02-02",
          "address": "New Address",
          "active": true
        }
        """;

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Maxi.Miliano"))
                .andExpect(jsonPath("$.firstName").value("NewFirst"))
                .andExpect(jsonPath("$.lastName").value("NewLast"))
                .andExpect(jsonPath("$.dateOfBirth").value("1992-02-02"))
                .andExpect(jsonPath("$.address").value("New Address"))
                .andExpect(jsonPath("$.active").value(true));

        verify(gymFacade).updateTraineeActiveStatus("Caller.User", "callerPass", "Maxi.Miliano", true);
    }

    @Test
    void updateTrainee_reconcilesActiveStatusInResponse_evenIfStaleInReturnedObject() throws Exception {
        // Simulates the exact scenario the reconciliation code handles: updateTrainee's
        // returned object still shows the OLD active value, since that call never touched it.
        User existingUser = new User("First", "Last", "Maxi.Miliano", "pass123", false);
        Trainee existingTrainee = new Trainee(existingUser, null, null);

        User staleUser = new User("First", "Last", "Maxi.Miliano", "pass123", false);
        Trainee staleUpdatedTrainee = new Trainee(staleUser, null, null);

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(existingTrainee);
        when(gymFacade.updateTrainee(eq("Caller.User"), eq("callerPass"), any(Trainee.class)))
                .thenReturn(staleUpdatedTrainee);

        String requestBody = """
        {
          "firstName": "First",
          "lastName": "Last",
          "active": true
        }
        """;

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(gymFacade).updateTraineeActiveStatus("Caller.User", "callerPass", "Maxi.Miliano", true);
    }

    @Test
    void updateTrainee_withOptionalFieldsOmitted_preservesExistingValues() throws Exception {
        User existingUser = new User("OldFirst", "OldLast", "Maxi.Miliano", "pass123", true);
        Trainee existingTrainee = new Trainee(existingUser, Date.valueOf("1990-01-01"), "Original Address");

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(existingTrainee);
        when(gymFacade.updateTrainee(eq("Caller.User"), eq("callerPass"), any(Trainee.class)))
                .thenReturn(existingTrainee);

        String requestBody = """
        {
          "firstName": "NewFirst",
          "lastName": "NewLast",
          "active": true
        }
        """;
        // dateOfBirth and address deliberately omitted

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(gymFacade).updateTrainee(eq("Caller.User"), eq("callerPass"), captor.capture());

        Trainee passedTrainee = captor.getValue();
        assertThat(passedTrainee.getDateOfBirth()).isEqualTo(Date.valueOf("1990-01-01"));
        assertThat(passedTrainee.getAddress()).isEqualTo("Original Address");
        assertThat(passedTrainee.getUser().getFirstName()).isEqualTo("NewFirst");
    }

    @Test
    void updateTrainee_withMissingFirstName_returns400() throws Exception {
        String invalidJson = "{\"lastName\":\"Last\",\"active\":true}";

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainee_withMissingActive_returns400() throws Exception {
        String invalidJson = "{\"firstName\":\"First\",\"lastName\":\"Last\"}";

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainee_withMissingAuthorizationHeader_returns401() throws Exception {
        String body = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"active\":true}";

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTrainee_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.getTrainee("Caller.User", "wrongPass", "Maxi.Miliano"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        String body = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"active\":true}";

        mockMvc.perform(put("/api/trainees/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTrainee_withNonexistentUsername_returns404() throws Exception {
        when(gymFacade.getTrainee("Caller.User", "callerPass", "Ghost.User"))
                .thenThrow(new NoSuchElementException("Trainee with username Ghost.User not found."));

        String body = "{\"firstName\":\"First\",\"lastName\":\"Last\",\"active\":true}";

        mockMvc.perform(put("/api/trainees/Ghost.User")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTraineeTrainersList_withValidRequest_returns200AndUpdatedList() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser1 = new User("Fran", "Miche", "Fran.Miche1", "pass1", true);
        Trainer trainer1 = new Trainer(trainerUser1, cardio);

        TrainingType yoga = new TrainingType("Yoga");
        User trainerUser2 = new User("Ana", "Lopez", "Ana.Lopez1", "pass2", true);
        Trainer trainer2 = new Trainer(trainerUser2, yoga);

        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "traineePass", true);
        Trainee trainee = new Trainee(traineeUser, null, null);
        trainee.setTrainers(List.of(trainer1, trainer2));

        when(gymFacade.updateTraineeTrainersList(
                eq("Caller.User"), eq("callerPass"), eq("Maxi.Miliano"), eq(List.of("Fran.Miche1", "Ana.Lopez1"))))
                .thenReturn(trainee);

        String requestBody = """
        {
          "trainersUsernames": ["Fran.Miche1", "Ana.Lopez1"]
        }
        """;

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers", hasSize(2)))
                .andExpect(jsonPath("$.trainers[0].username").value("Fran.Miche1"))
                .andExpect(jsonPath("$.trainers[0].specialization").value("Cardio"))
                .andExpect(jsonPath("$.trainers[1].username").value("Ana.Lopez1"))
                .andExpect(jsonPath("$.trainers[1].specialization").value("Yoga"));
    }

    @Test
    void updateTraineeTrainersList_withEmptyList_returns200AndEmptyTrainersList() throws Exception {
        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "traineePass", true);
        Trainee trainee = new Trainee(traineeUser, null, null);
        // trainers left empty — simulates removing all assigned trainers

        when(gymFacade.updateTraineeTrainersList(
                eq("Caller.User"), eq("callerPass"), eq("Maxi.Miliano"), eq(List.of())))
                .thenReturn(trainee);

        String requestBody = """
        {
          "trainersUsernames": []
        }
        """;

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers", hasSize(0)));
    }

    @Test
    void updateTraineeTrainersList_withNullTrainersUsernames_returns400() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTraineeTrainersList_withBlankUsernameInList_returns400() throws Exception {
        String invalidJson = "{\"trainersUsernames\": [\"Fran.Miche1\", \"   \"]}";

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTraineeTrainersList_withMissingAuthorizationHeader_returns401() throws Exception {
        String body = "{\"trainersUsernames\": [\"Fran.Miche1\"]}";

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTraineeTrainersList_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.updateTraineeTrainersList(
                eq("Caller.User"), eq("wrongPass"), eq("Maxi.Miliano"), eq(List.of("Fran.Miche1"))))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        String body = "{\"trainersUsernames\": [\"Fran.Miche1\"]}";

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTraineeTrainersList_withNonexistentTraineeUsername_returns404() throws Exception {
        when(gymFacade.updateTraineeTrainersList(
                eq("Caller.User"), eq("callerPass"), eq("Ghost.User"), eq(List.of("Fran.Miche1"))))
                .thenThrow(new NoSuchElementException("Trainee with username Ghost.User not found."));

        String body = "{\"trainersUsernames\": [\"Fran.Miche1\"]}";

        mockMvc.perform(put("/api/trainees/trainers-list/Ghost.User")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTraineeTrainersList_withNonexistentTrainerUsernameInList_returns404() throws Exception {
        when(gymFacade.updateTraineeTrainersList(
                eq("Caller.User"), eq("callerPass"), eq("Maxi.Miliano"), eq(List.of("Ghost.Trainer"))))
                .thenThrow(new NoSuchElementException("Trainer with username Ghost.Trainer not found."));

        String body = "{\"trainersUsernames\": [\"Ghost.Trainer\"]}";

        mockMvc.perform(put("/api/trainees/trainers-list/Maxi.Miliano")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTraineeTrainings_withAllFilters_returns200AndFilteredList() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "pass123", true);
        Trainer trainer = new Trainer(trainerUser, cardio);

        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "traineePass", true);
        Trainee trainee = new Trainee(traineeUser, null, null);

        Training training = new Training(trainee, trainer, "Morning Cardio", cardio,
                java.sql.Date.valueOf("2025-03-10"), 60);

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(trainee);

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        java.util.Date expectedFrom = java.util.Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
        java.util.Date expectedTo = java.util.Date.from(to.atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(gymFacade.getTraineeTrainings(
                eq("Caller.User"), eq("callerPass"), eq("Maxi.Miliano"),
                eq(expectedFrom), eq(expectedTo), eq("Fran"), eq("Cardio")))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainees/Maxi.Miliano/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass"))
                        .param("periodFrom", "2025-01-01")
                        .param("periodTo", "2025-12-31")
                        .param("trainerName", "Fran")
                        .param("trainingType", "Cardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                .andExpect(jsonPath("$[0].trainingDate").value("2025-03-10"))
                .andExpect(jsonPath("$[0].trainingType").value("Cardio"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].trainerName").value("Fran Miche"));
    }

    @Test
    void getTraineeTrainings_withNoFilters_returns200AndFullList() throws Exception {
        TrainingType cardio = new TrainingType("Cardio");
        User trainerUser = new User("Fran", "Miche", "Fran.Miche1", "pass123", true);
        Trainer trainer = new Trainer(trainerUser, cardio);

        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "traineePass", true);
        Trainee trainee = new Trainee(traineeUser, null, null);

        Training training = new Training(trainee, trainer, "Morning Cardio", cardio,
                java.sql.Date.valueOf("2025-03-10"), 60);

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(trainee);
        when(gymFacade.getTraineeTrainings(
                eq("Caller.User"), eq("callerPass"), eq("Maxi.Miliano"),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainees/Maxi.Miliano/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTraineeTrainings_withNoMatches_returns200AndEmptyList() throws Exception {
        User traineeUser = new User("Maxi", "Miliano", "Maxi.Miliano", "traineePass", true);
        Trainee trainee = new Trainee(traineeUser, null, null);

        when(gymFacade.getTrainee("Caller.User", "callerPass", "Maxi.Miliano"))
                .thenReturn(trainee);
        when(gymFacade.getTraineeTrainings(
                eq("Caller.User"), eq("callerPass"), eq("Maxi.Miliano"),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/Maxi.Miliano/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getTraineeTrainings_withMissingAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/trainees/Maxi.Miliano/trainings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTraineeTrainings_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.getTrainee("Caller.User", "wrongPass", "Maxi.Miliano"))
                .thenThrow(new SecurityException("Authentication failed for user: Caller.User"));

        mockMvc.perform(get("/api/trainees/Maxi.Miliano/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "wrongPass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTraineeTrainings_withNonexistentUsername_returns404() throws Exception {
        when(gymFacade.getTrainee("Caller.User", "callerPass", "Ghost.User"))
                .thenThrow(new NoSuchElementException("Trainee with username Ghost.User not found."));

        mockMvc.perform(get("/api/trainees/Ghost.User/trainings")
                        .header("Authorization", basicAuthHeader("Caller.User", "callerPass")))
                .andExpect(status().isNotFound());

        verify(gymFacade, never()).getTraineeTrainings(any(), any(), any(), any(), any(), any(), any());
    }
}