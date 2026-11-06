package com.example.trainer_workload_service;

import com.example.trainer_workload_service.controller.TrainerWorkloadController;
import com.example.trainer_workload_service.exception.GlobalExceptionHandler;
import com.example.trainer_workload_service.repository.TrainerWorkloadRepository;
import com.example.trainer_workload_service.security.JwtUtil;
import com.example.trainer_workload_service.security.SecurityConfig;
import com.example.trainer_workload_service.service.TrainerWorkloadServiceImpl;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@WebMvcTest(controllers = TrainerWorkloadController.class)
@Import({
        SecurityConfig.class,
        JwtUtil.class,
        GlobalExceptionHandler.class,
        TrainerWorkloadServiceImpl.class
})
@TestPropertySource(properties = "app.security.jwt.secret=test-secret-key-that-is-at-least-32-bytes-long")
class CucumberSpringConfiguration {

    @MockBean
    private TrainerWorkloadRepository repository;
}
