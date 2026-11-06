package gym.integrationbdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class MicroservicesIntegrationSteps {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration WORKLOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    private final IntegrationSettings settings = IntegrationSettings.load();
    private final Map<String, UserCredentials> users = new HashMap<>();

    private String authorization;
    private HttpResponse<String> lastResponse;
    private LocalDate trainingDate;
    private String trainingName;

    @Before
    public void resetScenario() {
        users.clear();
        authorization = null;
        lastResponse = null;
        trainingDate = null;
        trainingName = null;
    }

    @Given("a trainee named {string} is registered as {string}")
    public void registerTrainee(String name, String alias) throws Exception {
        HttpResponse<String> response = crmRequest("POST", "/api/trainees", null, Map.of(
                "firstName", name,
                "lastName", "IntegrationTest",
                "dateOfBirth", "1990-01-01",
                "address", "Integration test"
        ));
        assertStatus(response, HttpStatus.CREATED.value());
        JsonNode credentials = JSON.readTree(response.body());
        users.put(alias, new UserCredentials(
                credentials.path("username").asText(),
                credentials.path("password").asText()));
    }

    @Given("a trainer named {string} with specialization {string} is registered as {string}")
    public void registerTrainer(String name, String specialization, String alias) throws Exception {
        HttpResponse<String> response = crmRequest("POST", "/api/trainers", null, Map.of(
                "firstName", name,
                "lastName", "IntegrationTest",
                "specialization", specialization
        ));
        assertStatus(response, HttpStatus.CREATED.value());
        JsonNode credentials = JSON.readTree(response.body());
        users.put(alias, new UserCredentials(
                credentials.path("username").asText(),
                credentials.path("password").asText()));
    }

    @When("I authenticate as {string}")
    public void authenticate(String alias) throws Exception {
        UserCredentials user = user(alias);
        String basic = Base64.getEncoder().encodeToString(
                (user.username() + ":" + user.password()).getBytes(StandardCharsets.UTF_8));
        HttpResponse<String> response = send("POST", settings.crmBaseUrl() + "/api/auth/login",
                Map.of("Authorization", "Basic " + basic), null);
        assertStatus(response, HttpStatus.OK.value());
        authorization = "Bearer " + JSON.readTree(response.body()).path("token").asText();
    }

    @When("I create a future training named {string} of {int} minutes for trainee {string} with trainer {string}")
    public void createFutureTraining(
            String name, int duration, String traineeAlias, String trainerAlias) throws Exception {
        trainingName = name;
        trainingDate = LocalDate.now().plusDays(40);
        lastResponse = crmRequest("POST", "/api/trainings", authorization, trainingBody(
                traineeAlias, trainerAlias, name, trainingDate, duration));
    }

    @When("I try to create a training named {string} of {int} minutes for trainee {string} with trainer {string}")
    public void tryToCreateTraining(
            String name, int duration, String traineeAlias, String trainerAlias) throws Exception {
        trainingName = name;
        trainingDate = LocalDate.now().plusDays(40);
        lastResponse = crmRequest("POST", "/api/trainings", authorization, trainingBody(
                traineeAlias, trainerAlias, name, trainingDate, duration));
    }

    @When("I delete the created training")
    public void deleteCreatedTraining() throws Exception {
        assertThat(trainingName).as("training was created in this scenario").isNotBlank();
        String trainingId = findTrainingId(trainingName);
        lastResponse = crmRequest("DELETE", "/api/trainings/" + trainingId, authorization, null);
    }

    @Then("the CRM should accept the request with status {int}")
    public void crmShouldAcceptRequest(int expectedStatus) {
        assertThat(lastResponse).as("CRM request response").isNotNull();
        assertStatus(lastResponse, expectedStatus);
    }

    @Then("the CRM should reject the request with status {int}")
    public void crmShouldRejectRequest(int expectedStatus) {
        assertThat(lastResponse).as("CRM request response").isNotNull();
        assertStatus(lastResponse, expectedStatus);
    }

    @Then("the CRM monthly workload for trainer {string} should eventually be {double} hour(s)")
    public void crmMonthlyWorkloadShouldEventuallyBe(String trainerAlias, double expectedTotal) throws Exception {
        String username = user(trainerAlias).username();
        awaitWorkload(() -> crmRequest("GET", monthlyPath(username), authorization, null),
                response -> response.statusCode() == HttpStatus.OK.value()
                        && closeTo(totalDuration(response), expectedTotal),
                "CRM monthly workload total " + expectedTotal + " for " + username);
    }

    @Then("the CRM full workload for trainer {string} should eventually include the session")
    public void crmFullWorkloadShouldEventuallyIncludeSession(String trainerAlias) throws Exception {
        String username = user(trainerAlias).username();
        awaitWorkload(() -> crmRequest(
                        "GET", "/api/trainers/" + username + "/workload", authorization, null),
                response -> response.statusCode() == HttpStatus.OK.value()
                        && containsYearAndMonth(response, trainingDate),
                "full workload containing " + trainingDate.getYear() + "/" + trainingDate.getMonthValue());
    }

    @Then("the workload service should reject unauthenticated and invalid JWT requests with status {int}")
    public void workloadServiceShouldRejectInvalidJwt(int expectedStatus) throws Exception {
        HttpResponse<String> missingTokenResponse = send(
                "GET",
                settings.workloadBaseUrl() + "/api/trainers/unknown.trainer/workload",
                Map.of(),
                null);
        assertStatus(missingTokenResponse, expectedStatus);
        HttpResponse<String> invalidTokenResponse = send(
                "GET",
                settings.workloadBaseUrl() + "/api/trainers/unknown.trainer/workload",
                Map.of("Authorization", "Bearer invalid.integration.token"),
                null);
        assertStatus(invalidTokenResponse, expectedStatus);
    }

    @Then("the workload service should not have a record for trainer {string}")
    public void workloadServiceShouldNotHaveRecord(String trainerAlias) throws Exception {
        String username = user(trainerAlias).username();
        String url = settings.workloadBaseUrl() + "/api/trainers/" + username + "/workload";
        HttpResponse<String> response = send(
                "GET", url, Map.of("Authorization", "Bearer " + serviceToken()), null);
        assertStatus(response, HttpStatus.NOT_FOUND.value());
    }

    private Map<String, Object> trainingBody(
            String traineeAlias, String trainerAlias, String name, LocalDate date, int duration) {
        return Map.of(
                "traineeUsername", user(traineeAlias).username(),
                "trainerUsername", user(trainerAlias).username(),
                "name", name,
                "date", date.toString(),
                "duration", duration
        );
    }

    private String monthlyPath(String username) {
        return "/api/trainers/" + username + "/workload/"
                + trainingDate.getYear() + "/" + trainingDate.getMonthValue();
    }

    private HttpResponse<String> crmRequest(
            String method, String path, String token, Map<String, ?> body) throws Exception {
        Map<String, String> headers = token == null ? Map.of() : Map.of("Authorization", token);
        return send(method, settings.crmBaseUrl() + path, headers, body);
    }

    private HttpResponse<String> send(
            String method, String url, Map<String, String> headers, Map<String, ?> body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
        headers.forEach(request::header);
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void awaitWorkload(
            CheckedResponse request, ResponseCondition condition, String expectation) throws Exception {
        long deadline = System.nanoTime() + WORKLOAD_TIMEOUT.toNanos();
        HttpResponse<String> last = null;
        while (System.nanoTime() < deadline) {
            last = request.get();
            if (condition.matches(last)) {
                lastResponse = last;
                return;
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
        String details = last == null ? "no response" : last.statusCode() + " " + last.body();
        throw new AssertionError("Timed out waiting for " + expectation + "; last response: " + details);
    }

    private double totalDuration(HttpResponse<String> response) throws IOException {
        return JSON.readTree(response.body()).path("totalDuration").asDouble(Double.NaN);
    }

    private boolean containsYearAndMonth(HttpResponse<String> response, LocalDate date) throws IOException {
        JsonNode years = JSON.readTree(response.body()).path("years");
        for (JsonNode year : years) {
            if (year.path("year").asInt() != date.getYear()) {
                continue;
            }
            for (JsonNode month : year.path("months")) {
                if (month.path("month").asInt() == date.getMonthValue()
                        && month.path("trainingSummaryDuration").asDouble() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private String findTrainingId(String name) throws SQLException {
        String normalizedPath = Path.of(settings.crmDatabasePath())
                .toAbsolutePath().toString().replace('\\', '/');
        String url = "jdbc:h2:file:" + normalizedPath
                + ";AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement(
                     "SELECT training_id FROM trainings WHERE training_name = ?")) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("CRM did not persist training: " + name);
                }
                return result.getString(1);
            }
        }
    }

    private String serviceToken() {
        byte[] key = settings.workloadJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Jwts.builder()
                .setSubject("gym-task")
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
                .compact();
    }

    private static boolean closeTo(double actual, double expected) {
        return Double.isFinite(actual) && Math.abs(actual - expected) < 0.0001;
    }

    private static void assertStatus(HttpResponse<String> response, int expectedStatus) {
        assertThat(response.statusCode())
                .as("HTTP response body: %s", response.body())
                .isEqualTo(expectedStatus);
    }

    private UserCredentials user(String alias) {
        return Objects.requireNonNull(users.get(alias), "No user is registered as " + alias);
    }

    private record IntegrationSettings(
            String crmBaseUrl, String workloadBaseUrl, String workloadJwtSecret, String crmDatabasePath) {

        private static IntegrationSettings load() {
            String statePath = System.getProperty("integration.state.file");
            if (statePath == null || statePath.isBlank()) {
                throw new IllegalStateException("The Maven integration profile must provide integration.state.file.");
            }
            Properties properties = new Properties();
            try (var input = java.nio.file.Files.newInputStream(Path.of(statePath))) {
                properties.load(input);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot load integration environment state from " + statePath, exception);
            }
            return new IntegrationSettings(
                    properties.getProperty("crm.base.url"),
                    properties.getProperty("workload.base.url"),
                    properties.getProperty("workload.jwt.secret"),
                    properties.getProperty("crm.database.path"));
        }
    }

    @FunctionalInterface
    private interface CheckedResponse {
        HttpResponse<String> get() throws Exception;
    }

    @FunctionalInterface
    private interface ResponseCondition {
        boolean matches(HttpResponse<String> response) throws Exception;
    }

    private record UserCredentials(String username, String password) {
    }
}
