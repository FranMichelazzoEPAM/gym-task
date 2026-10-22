package com.example.trainer_workload_service.controller;

import com.example.trainer_workload_service.dto.MonthlyWorkloadResponse;
import com.example.trainer_workload_service.exception.GlobalExceptionHandler;
import com.example.trainer_workload_service.exception.TrainerNotFoundException;
import com.example.trainer_workload_service.security.JwtUtil;
import com.example.trainer_workload_service.security.SecurityConfig;
import com.example.trainer_workload_service.service.TrainerWorkloadService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
@Import({SecurityConfig.class, JwtUtil.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "app.security.jwt.secret=test-secret-key-that-is-at-least-32-bytes-long")
class TrainerWorkloadControllerTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerWorkloadService service;

    @Test
    void returnsMonthlySummary() throws Exception {
        when(service.getMonthlySummary("trainer1", 2025, 3))
                .thenReturn(new MonthlyWorkloadResponse("trainer1", 2025, 3, 2.5));

        mockMvc.perform(get("/api/trainers/trainer1/workload/2025/3")
                        .header("Authorization", bearer(validToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("trainer1"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.totalDuration").value(2.5));
    }

    @Test
    void mapsUnknownTrainerToNotFound() throws Exception {
        when(service.getMonthlySummary("missing", 2025, 3))
                .thenThrow(new TrainerNotFoundException("missing"));

        mockMvc.perform(get("/api/trainers/missing/workload/2025/3")
                        .header("Authorization", bearer(validToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsRequestsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/trainers/trainer1/workload/2025/3"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/trainers/trainer1/workload/2025/3")
                        .header("Authorization", bearer("not-a-jwt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredJwt() throws Exception {
        mockMvc.perform(get("/api/trainers/trainer1/workload/2025/3")
                        .header("Authorization", bearer(expiredToken())))
                .andExpect(status().isUnauthorized());
    }

    private String validToken() {
        return Jwts.builder()
                .subject("test-user")
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(secretKey())
                .compact();
    }

    private String expiredToken() {
        return Jwts.builder()
                .subject("test-user")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(secretKey())
                .compact();
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
