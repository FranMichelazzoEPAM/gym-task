package gym.client;

import gym.client.dto.TrainerWorkloadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import gym.client.dto.MonthlyWorkloadResponse;

@FeignClient(name = "trainer-workload-service", configuration = TrainerWorkloadQueryClientConfig.class)
public interface TrainerWorkloadQueryClient {

    @GetMapping("/api/trainers/{username}/workload/{year}/{month}")
    MonthlyWorkloadResponse getMonthlySummary(
            @PathVariable("username") String username,
            @PathVariable("year") int year,
            @PathVariable("month") int month);

    @GetMapping("/api/trainers/{username}/workload")
    TrainerWorkloadResponse getTrainerWorkload(
            @PathVariable("username") String username);
}