package gym.security;

import gym.service.TraineeService;
import gym.service.TrainerService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthenticationAspect {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuthenticationAspect.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Autowired
    public AuthenticationAspect(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Before("@annotation(gym.security.RequiresAuthentication) && args(username, password, ..)")
    public void checkAuthentication(String username, String password) {
        boolean authenticated = traineeService.authenticate(username, password)
                || trainerService.authenticate(username, password);

        if (!authenticated) {
            LOG.warn("Authentication failed for user: {}", username);
            throw new SecurityException("Authentication failed for user: " + username);
        }
        LOG.debug("Authentication succeeded for user: {}", username);
    }
}