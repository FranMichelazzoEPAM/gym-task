package gym.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import gym.client.TrainerWorkloadNotifier;
import gym.client.TrainerWorkloadQueryClient;
import gym.client.dto.MonthlyWorkloadResponse;
import gym.client.dto.TrainerWorkloadResponse;
import gym.domain.Training;
import gym.domain.TrainingType;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingRepository;
import gym.repository.TrainingTypeRepository;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class GymApiSteps {

    private static final Pattern ALIAS_PLACEHOLDER =
            Pattern.compile("\\$\\{([A-Za-z0-9_-]+)\\.(username|password)}");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainerWorkloadNotifier trainerWorkloadNotifier;

    @Autowired
    private TrainerWorkloadQueryClient trainerWorkloadQueryClient;

    private final Map<String, UserCredentials> users = new HashMap<>();
    private String jwt;
    private int responseStatus;
    private String responseBody = "";

    @Before
    public void resetScenario() {
        Mockito.reset(trainerWorkloadNotifier, trainerWorkloadQueryClient);
        jdbcTemplate.update("DELETE FROM trainee_trainers");
        trainingRepository.deleteAll();
        traineeRepository.deleteAll();
        trainerRepository.deleteAll();
        trainingTypeRepository.deleteAll();
        users.clear();
        jwt = null;
        responseStatus = 0;
        responseBody = "";
    }

    @Given("training type {string} exists")
    public void trainingTypeExists(String name) {
        trainingTypeRepository.findByTrainingTypeName(name)
                .orElseGet(() -> trainingTypeRepository.save(new TrainingType(name)));
    }

    @Given("a trainee named {string} exists as {string}")
    public void traineeExists(String name, String alias) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "firstName", name,
                "lastName", "ComponentTest",
                "dateOfBirth", "1990-01-01",
                "address", "BDD test"
        ));
        MvcResult result = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        rememberRegisteredUser(alias, result.getResponse());
    }

    @Given("a trainer named {string} with specialization {string} exists as {string}")
    public void trainerExists(String name, String specialization, String alias) throws Exception {
        trainingTypeExists(specialization);
        String body = objectMapper.writeValueAsString(Map.of(
                "firstName", name,
                "lastName", "ComponentTest",
                "specialization", specialization
        ));
        MvcResult result = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        rememberRegisteredUser(alias, result.getResponse());
    }

    @Given("I am logged in as {string}")
    public void iAmLoggedIn(String alias) throws Exception {
        login(alias, credential(alias).password());
    }

    @When("I log in as {string} with a valid password")
    public void loginWithValidPassword(String alias) throws Exception {
        login(alias, credential(alias).password());
    }

    @When("I log in as {string} with an invalid password")
    public void loginWithInvalidPassword(String alias) throws Exception {
        login(alias, "incorrect-password");
    }

    @When("I log in as {string} with password {string}")
    public void loginWithPassword(String alias, String password) throws Exception {
        login(alias, expand(password));
    }

    @When("I submit three invalid login attempts and then a valid one as {string}")
    public void accountLocksAfterRepeatedInvalidLogins(String alias) throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            login(alias, "incorrect-password");
        }
        login(alias, credential(alias).password());
    }

    @When("I send a {word} request to {string} with JSON:")
    public void sendRequestWithJson(String method, String path, String body) throws Exception {
        sendRequest(method, path, body, true);
    }

    @When("I send a {word} request to {string}")
    public void sendRequestWithoutBody(String method, String path) throws Exception {
        sendRequest(method, path, null, true);
    }

    @When("I send an unauthenticated {word} request to {string}")
    public void sendUnauthenticatedRequest(String method, String path) throws Exception {
        sendRequest(method, path, null, false);
    }

    @When("I delete the created training")
    public void deleteCreatedTraining() throws Exception {
        Training training = trainingRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected a training to delete"));
        sendRequest("DELETE", "/api/trainings/" + training.getTrainingId(), null, true);
    }

    @Given("the workload service returns a monthly total of {double} for trainer {string} in {int} month {int}")
    public void workloadServiceReturnsMonthlyTotal(
            double total, String alias, int month, int year) {
        String username = credential(alias).username();
        when(trainerWorkloadQueryClient.getMonthlySummary(username, year, month))
                .thenReturn(new MonthlyWorkloadResponse(username, year, month, total));
    }

    @Given("the workload service is unavailable for trainer {string}")
    public void workloadServiceIsUnavailable(String alias) {
        String username = credential(alias).username();
        when(trainerWorkloadQueryClient.getTrainerWorkload(username))
                .thenThrow(new RuntimeException("workload service unavailable"));
    }

    @Given("the workload service returns a summary for trainer {string}")
    public void workloadServiceReturnsSummary(String alias) {
        String username = credential(alias).username();
        when(trainerWorkloadQueryClient.getTrainerWorkload(username))
                .thenReturn(new TrainerWorkloadResponse(
                        username, "Workload", "Summary", true, List.of()));
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        assertThat(responseStatus).isEqualTo(expectedStatus);
    }

    @Then("the response JSON field {string} should equal {string}")
    public void responseJsonFieldShouldEqual(String path, String expected) {
        Object actual = JsonPath.read(responseBody, path);
        assertThat(String.valueOf(actual)).isEqualTo(expand(expected));
    }

    @Then("the response JSON field {string} should not be empty")
    public void responseJsonFieldShouldNotBeEmpty(String path) {
        Object actual = JsonPath.read(responseBody, path);
        assertThat(String.valueOf(actual)).isNotBlank();
    }

    @Then("the response JSON should contain {int} items")
    public void responseJsonShouldContainItems(int expectedCount) {
        Object actual = JsonPath.read(responseBody, "$");
        assertThat(actual).asList().hasSize(expectedCount);
    }

    @Then("the response body should include {string}")
    public void responseBodyShouldInclude(String expected) {
        assertThat(responseBody).contains(expected);
    }

    @Then("the database should contain {int} trainings")
    public void databaseShouldContainTrainings(int expectedCount) {
        assertThat(trainingRepository.count()).isEqualTo(expectedCount);
    }

    private void rememberRegisteredUser(String alias, MockHttpServletResponse response) throws Exception {
        assertThat(response.getStatus())
                .as("registration response: %s", response.getContentAsString())
                .isEqualTo(201);
        JsonNode credentials = objectMapper.readTree(response.getContentAsString());
        users.put(alias, new UserCredentials(
                credentials.path("username").asText(),
                credentials.path("password").asText()));
    }

    private void login(String alias, String password) throws Exception {
        UserCredentials user = credential(alias);
        String rawCredentials = user.username() + ":" + password;
        String basic = Base64.getEncoder().encodeToString(rawCredentials.getBytes(StandardCharsets.UTF_8));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Basic " + basic))
                .andReturn();
        recordResponse(result);
        jwt = null;
        if (responseStatus == 200) {
            jwt = objectMapper.readTree(responseBody).path("token").asText();
        }
    }

    private void sendRequest(String method, String path, String body, boolean includeJwt) throws Exception {
        String expandedPath = expand(path);
        MockHttpServletRequestBuilder request = switch (method.toUpperCase()) {
            case "GET" -> get(expandedPath);
            case "POST" -> post(expandedPath);
            case "PUT" -> put(expandedPath);
            case "PATCH" -> patch(expandedPath);
            case "DELETE" -> delete(expandedPath);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
        if (includeJwt && jwt != null) {
            request.header("Authorization", "Bearer " + jwt);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(expand(body));
        }
        recordResponse(mockMvc.perform(request).andReturn());
    }

    private void recordResponse(MvcResult result) throws Exception {
        responseStatus = result.getResponse().getStatus();
        responseBody = result.getResponse().getContentAsString();
    }

    private UserCredentials credential(String alias) {
        UserCredentials user = users.get(alias);
        if (user == null) {
            throw new IllegalArgumentException("No registered user exists for alias: " + alias);
        }
        return user;
    }

    private String expand(String value) {
        Matcher matcher = ALIAS_PLACEHOLDER.matcher(value);
        StringBuffer expanded = new StringBuffer();
        while (matcher.find()) {
            UserCredentials user = credential(matcher.group(1));
            String replacement = matcher.group(2).equals("username") ? user.username() : user.password();
            matcher.appendReplacement(expanded, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(expanded);
        return expanded.toString();
    }

    private record UserCredentials(String username, String password) {
    }
}
