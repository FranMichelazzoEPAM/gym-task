package gym.security.jwt;

import gym.domain.Trainee;
import gym.domain.User;
import gym.repository.TraineeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String username;

    @BeforeEach
    void createUser() {
        username = "jwt-test-" + UUID.randomUUID();
        User user = new User("JWT", "Test", username, passwordEncoder.encode("test-password"), true);
        traineeRepository.save(new Trainee(user, null, null));
    }

    @Test
    void protectedEndpoint_rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_acceptsValidToken() throws Exception {
        String token = jwtUtil.generateToken(username, "TRAINEE");

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_rejectsNonBearerAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_rejectsBlacklistedToken() throws Exception {
        String token = jwtUtil.generateToken(username, "TRAINEE");
        tokenBlacklistService.blacklist(token);

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_blacklistsTokenAndRejectsItsReuse() throws Exception {
        String token = jwtUtil.generateToken(username, "TRAINEE");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_rejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_rejectsNonBearerAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Basic credentials"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_rejectsMalformedBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }
}
