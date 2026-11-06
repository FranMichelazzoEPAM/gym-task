package com.example.trainer_workload_service.steps;

import com.example.trainer_workload_service.domain.MonthSummary;
import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.example.trainer_workload_service.domain.YearSummary;
import com.example.trainer_workload_service.repository.TrainerWorkloadRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainerWorkloadStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    private final Map<String, TrainerWorkload> trainers = new HashMap<>();
    private ResultActions response;

    @Before
    public void resetScenarioState() {
        trainers.clear();
        response = null;
        reset(repository);
        when(repository.findById(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(trainers.get(invocation.getArgument(0))));
    }

    @Given("trainer {string} has {double} training hours in month {int} of {int}")
    public void trainerHasRecordedTraining(String username, double duration, int month, int year) {
        TrainerWorkload trainer = new TrainerWorkload(username, "Taylor", "Trainer", true);
        YearSummary yearSummary = new YearSummary(year);
        yearSummary.getMonths().add(new MonthSummary(month, duration));
        trainer.setYears(new ArrayList<>(java.util.List.of(yearSummary)));
        trainers.put(username, trainer);
    }

    @When("the client requests monthly workload for trainer {string} in {int}-{int} with a valid JWT")
    public void requestMonthlyWithValidJwt(String username, int year, int month) throws Exception {
        requestMonthly(username, year, month, validToken());
    }

    @When("the client requests monthly workload for trainer {string} in {int}-{int} without a JWT")
    public void requestMonthlyWithoutJwt(String username, int year, int month) throws Exception {
        response = mockMvc.perform(get(monthlyPath(), username, year, month));
    }

    @When("the client requests monthly workload for trainer {string} in {int}-{int} with an invalid JWT")
    public void requestMonthlyWithInvalidJwt(String username, int year, int month) throws Exception {
        requestMonthly(username, year, month, "not-a-jwt");
    }

    @When("the client requests monthly workload for trainer {string} in {int}-{int} with an expired JWT")
    public void requestMonthlyWithExpiredJwt(String username, int year, int month) throws Exception {
        requestMonthly(username, year, month, tokenExpiringIn(-60));
    }

    @When("the client requests complete workload for trainer {string} with a valid JWT")
    public void requestCompleteWorkloadWithValidJwt(String username) throws Exception {
        response = mockMvc.perform(get("/api/trainers/{username}/workload", username)
                .header("Authorization", bearer(validToken())));
    }

    @When("the client requests complete workload for trainer {string} without a JWT")
    public void requestCompleteWorkloadWithoutJwt(String username) throws Exception {
        response = mockMvc.perform(get("/api/trainers/{username}/workload", username));
    }

    @When("the client requests complete workload for trainer {string} with an invalid JWT")
    public void requestCompleteWorkloadWithInvalidJwt(String username) throws Exception {
        response = mockMvc.perform(get("/api/trainers/{username}/workload", username)
                .header("Authorization", bearer("not-a-jwt")));
    }

    @When("the client requests complete workload for trainer {string} with an expired JWT")
    public void requestCompleteWorkloadWithExpiredJwt(String username) throws Exception {
        response = mockMvc.perform(get("/api/trainers/{username}/workload", username)
                .header("Authorization", bearer(tokenExpiringIn(-60))));
    }

    @Then("the response status is {int}")
    public void responseHasStatus(int expectedStatus) throws Exception {
        response.andExpect(status().is(expectedStatus));
    }

    @Then("the monthly summary identifies trainer {string}, year {int}, month {int}, with {double} total hours")
    public void responseContainsMonthlySummary(
            String username, int year, int month, double totalHours) throws Exception {
        response.andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.year").value(year))
                .andExpect(jsonPath("$.month").value(month))
                .andExpect(jsonPath("$.totalDuration").value(totalHours));
    }

    @Then("the complete workload identifies trainer {string} with name {string} {string} and {double} hours in month {int} of {int}")
    public void responseContainsCompleteWorkload(
            String username, String firstName, String lastName, double duration, int month, int year)
            throws Exception {
        response.andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.years[0].year").value(year))
                .andExpect(jsonPath("$.years[0].months[0].month").value(month))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(duration));
    }

    @Then("the error identifies status {int} and trainer {string}")
    public void responseContainsNotFoundError(int statusCode, String username) throws Exception {
        response.andExpect(jsonPath("$.status").value(statusCode))
                .andExpect(jsonPath("$.message").value("Trainer not found: " + username));
    }

    private void requestMonthly(String username, int year, int month, String token) throws Exception {
        response = mockMvc.perform(get(monthlyPath(), username, year, month)
                .header("Authorization", bearer(token)));
    }

    private String monthlyPath() {
        return "/api/trainers/{username}/workload/{year}/{month}";
    }

    private String validToken() {
        return tokenExpiringIn(300);
    }

    private String tokenExpiringIn(long seconds) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("component-test-user")
                .expiration(Date.from(Instant.now().plusSeconds(seconds)))
                .signWith(key)
                .compact();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
