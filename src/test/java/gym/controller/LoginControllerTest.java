package gym.controller;

import gym.facade.GymFacade;
import gym.testutil.MetricsTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@Import(MetricsTestConfig.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Test
    void login_withValidTraineeCredentials_returns200() throws Exception {
        when(gymFacade.authenticateTrainee("Fran.Miche", "KawgEonxI6")).thenReturn(true);
        when(gymFacade.authenticateTrainer("Fran.Miche", "KawgEonxI6")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuthHeader("Fran.Miche", "KawgEonxI6")))
                .andExpect(status().isOk());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(gymFacade.authenticateTrainee("Fran.Miche", "wrongPassword")).thenReturn(false);
        when(gymFacade.authenticateTrainer("Fran.Miche", "wrongPassword")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuthHeader("Fran.Miche", "wrongPassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withMissingAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withMalformedAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", "Bearer sometoken"))
                .andExpect(status().isUnauthorized());
    }
}