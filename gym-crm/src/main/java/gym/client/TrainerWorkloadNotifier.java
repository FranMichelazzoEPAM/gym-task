package gym.client;

import gym.client.dto.TrainerWorkloadRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class TrainerWorkloadNotifier {

    private final ApplicationEventPublisher eventPublisher;

    public TrainerWorkloadNotifier(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notifyWorkload(TrainerWorkloadRequest request) {
        eventPublisher.publishEvent(new WorkloadNotificationEvent(request));
    }
}
