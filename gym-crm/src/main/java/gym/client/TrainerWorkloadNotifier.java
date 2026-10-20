package gym.client;

import gym.client.dto.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TrainerWorkloadNotifier {

    public static final Logger LOG =  LoggerFactory.getLogger(TrainerWorkloadNotifier.class.getName());

    private final TrainerWorkloadClient client;

    public TrainerWorkloadNotifier(TrainerWorkloadClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "trainerWorkloadService", fallbackMethod = "fallback")
    public void notifyWorkload(TrainerWorkloadRequest request) {
        client.sendWorkload(request);
    }

    private void fallback(TrainerWorkloadRequest request, Throwable throwable) {
        LOG.error("Failed to notify Trainer Workload Service for trainer={} action={} | reason={}",
                request.username(), request.actionType(), throwable.getMessage());
    }
}
