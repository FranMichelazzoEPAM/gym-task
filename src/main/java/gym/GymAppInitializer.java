package gym;

import gym.config.AppConfig;
import gym.domain.Trainee;
import gym.facade.GymFacade;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GymAppInitializer {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade gymFacade = context.getBean(GymFacade.class);

        System.out.println("Loaded trainees:");
        for (Trainee trainee : gymFacade.getAllTrainees()) {
            System.out.println(trainee.getFirstName() + " "
                    + trainee.getLastName());
        }
    }
}
