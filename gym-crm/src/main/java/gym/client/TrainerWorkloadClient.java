package gym.client;

import gym.client.dto.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload-service", configuration = FeignClientConfig.class)
public interface TrainerWorkloadClient {
    @PostMapping("/api/trainers/workload")
    void sendWorkload(@RequestBody TrainerWorkloadRequest request);
}
