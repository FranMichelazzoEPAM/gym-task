package gym;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.TrainingType;
import gym.facade.GymFacade;
import gym.repository.TrainingTypeRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class GymAppInitializer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(GymAppInitializer.class, args);

        GymFacade gymFacade = context.getBean(GymFacade.class);
        TrainingTypeRepository trainingTypeRepository = context.getBean(TrainingTypeRepository.class);


        Date appInitDate = new Date();
        // #1: Create a new Trainer profile
        List<TrainingType> allTypes = trainingTypeRepository.findAll();
        TrainingType cardio = allTypes.stream()
                .filter(t -> t.getTrainingTypeName().equals("Cardio"))
                .findFirst()
                .orElseThrow();

        TrainingType pilates = allTypes.stream()
                .filter(t -> t.getTrainingTypeName().equals("Pilates"))
                .findFirst()
                .orElseThrow();

        Trainer trainer1 = gymFacade.createTrainer("Santi", "Stutz", List.of(cardio));
        System.out.println("Created trainer: " + trainer1);

        System.out.println("Creating another trainer...");
        Trainer trainer2 = gymFacade.createTrainer("Maxi", "Miliano",
                List.of(cardio, pilates));
        System.out.println("Created trainer: " + trainer2);

        // #2: Create a new Trainee profile
        Trainee trainee1 = gymFacade.createTrainee("Fran", "Miche", null, null);
        System.out.println("Created trainee: " + trainee1);

        System.out.println("Creating another trainee with the same name and lastname...");
        Trainee trainee2 = gymFacade.createTrainee("Fran", "Miche", null, null);
        System.out.println("Created another trainee: " + trainee2);

        // #5: Select Trainer profile by username
        Trainer selectedTrainer = gymFacade.getTrainer(
                trainee1.getUser().getUsername(),
                trainee1.getUser().getPassword(),
                trainer1.getUser().getUsername());
        System.out.println(selectedTrainer);

        // #6: Select Trainee profile by username
        Trainee selectedTrainee = gymFacade.getTrainee(
          trainer1.getUser().getUsername(),
          trainer1.getUser().getPassword(),
          trainee1.getUser().getUsername()
        );
        System.out.println(selectedTrainee);

        // #7: Trainee password change
        gymFacade.changeTraineePassword(trainee1.getUser().getUsername(),
                trainee1.getUser().getPassword(),
                "newPassword");

        // #8: Trainer password change
        gymFacade.changeTrainerPassword(trainer1.getUser().getUsername(),
                trainer1.getUser().getPassword(),
                "newPassword");

        // #9: Update trainer profile
        trainer2.getUser().setFirstName("Guillermo");
        gymFacade.updateTrainer(trainer2.getUser().getUsername(),
                trainer2.getUser().getPassword(),
                trainer2);

        // #10: Update trainee profile
        trainer2.getUser().setFirstName("newFirstName");
        gymFacade.updateTrainee(
                trainee2.getUser().getUsername(),
                trainee2.getUser().getPassword(),
                trainee2);

        // #11: Activate/De-activate trainee
        gymFacade.toggleTraineeActiveStatus(
                trainer2.getUser().getUsername(),
                trainer2.getUser().getPassword(),
                trainee1.getUser().getUsername());

        // #12: Activate/De-activate trainer
        gymFacade.toggleTrainerActiveStatus(
                trainer2.getUser().getUsername(),
                trainer2.getUser().getPassword(),
                trainer1.getUser().getUsername());

        // #13: Delete trainee profile by username
        gymFacade.deleteTrainee(trainee2.getUser().getUsername(),
                trainee2.getUser().getPassword(),
                trainee1.getUser().getUsername());

        // #16: Add training
        Date trainingDate = new Date();
        gymFacade.createTraining(trainee2.getUser().getUsername(),
                trainee2.getUser().getPassword(),
                trainee2.getUser().getUsername(),
                trainer2.getUser().getUsername(),
                "First exercise",
                cardio,
                trainingDate,
                45);

        // #14 Get trainee trainings list by trainee username and criteria
        // (from date, to date, trainer name, trining type)
        Date beforeSearchDate = new Date();
        gymFacade.getTraineeTrainings(
                trainee2.getUser().getUsername(),
                trainee2.getUser().getPassword(),
                trainee2.getUser().getUsername(),
                appInitDate, beforeSearchDate,
                "Maximiliano",
                "Cardio");

        // #15: Get trainer trainings list by trainer username and criteria
        // (from date, to date, trainee name, trining type)
        gymFacade.getTrainerTrainings(
                trainer2.getUser().getUsername(),
                trainer2.getUser().getPassword(),
                trainer2.getUser().getUsername(),
                appInitDate, beforeSearchDate,
                "Fran");

        // #17: Get trainers list that not assigned on trainee by trainee's username
        gymFacade.getTrainersNotAssignedToTrainee(trainee2.getUser().getUsername(),
                trainee2.getUser().getPassword(),
                trainee2.getUser().getUsername());

        // #18: Update Trainee's trainers list
        List<String> newTrainers = new ArrayList<>();
        newTrainers.add(trainer2.getUser().getUsername());
        gymFacade.updateTraineeTrainersList(
                trainee2.getUser().getUsername(),
                trainee2.getUser().getPassword(),
                trainee2.getUser().getUsername(),
                newTrainers);

        context.close();
    }
}

