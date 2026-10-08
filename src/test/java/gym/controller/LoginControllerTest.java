package gym.controller;

import gym.domain.User;
import gym.facade.GymFacade;
import gym.security.jwt.TokenBlacklistService;
import gym.security.userDetails.CustomUserDetails;
import gym.security.jwt.JwtUtil;
import gym.security.service.CustomUserDetailsService;
import gym.testutil.MetricsTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MetricsTestConfig.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Test
    void login_withValidTraineeCredentials_returns200AndToken() throws Exception {
        User user = new User("Fran", "Miche", "Fran.Miche", "hashedPassword", true);
        CustomUserDetails userDetails = new CustomUserDetails(user, "TRAINEE");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("Fran.Miche", "TRAINEE")).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuthHeader("Fran.Miche", "KawgEonxI6")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuthHeader("Fran.Miche", "wrongPassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withLockedAccount_returns423() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("Account locked"));

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuthHeader("Fran.Miche", "KawgEonxI6")))
                .andExpect(status().isLocked());
    }

    @Test
    void login_withMissingAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withMalformedAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer sometoken"))
                .andExpect(status().isUnauthorized());
    }
}