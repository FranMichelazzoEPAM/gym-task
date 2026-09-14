package gym.repository;

import gym.domain.*;
import gym.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("save trainer and find by username")
    void saveAndFindByUserUsername() {
        TrainingType type = trainingTypeRepository.findByTrainingTypeName("Crossfit").orElseGet(() -> trainingTypeRepository.save(TestDataFactory.trainingType("Crossfit")));
        Trainer trainer = trainerRepository.save(TestDataFactory.trainer("Anna","Bell","ann","pwd",true));
        assertThat(trainer.getTrainerId()).isNotNull();

        var found = trainerRepository.findByUser_Username("ann");
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUsername()).isEqualTo("ann");
    }

    @Test
    @DisplayName("find unassigned trainers for trainee")
    void findUnassignedTrainersForTrainee() {
        Trainer t1 = trainerRepository.save(TestDataFactory.trainer("T1","One","t1","p1",true));
        Trainer t2 = trainerRepository.save(TestDataFactory.trainer("T2","Two","t2","p2",true));
        Trainee trainee = traineeRepository.save(TestDataFactory.trainee("Jake","Long","jake","pw",true));

        // assign t1 to trainee
        trainee.getTrainers().add(t1);
        traineeRepository.save(trainee);

        List<Trainer> unassigned = trainerRepository.findUnassignedTrainersForTrainee("jake");
        assertThat(unassigned).extracting(tr -> tr.getUser().getUsername()).containsExactly("t2");
    }
}
