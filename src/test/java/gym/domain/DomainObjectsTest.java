package gym.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

public class DomainObjectsTest {

    @Test
    public void testUserGettersAndToString() {
        User u = new User("First","Last","user123","pass",true);
        Assertions.assertEquals("First", u.getFirstName());
        Assertions.assertEquals("Last", u.getLastName());
        Assertions.assertEquals("user123", u.getUsername());
        Assertions.assertTrue(u.isActive());
        String s = u.toString();
        Assertions.assertTrue(s.contains("First"));
        Assertions.assertTrue(s.contains("Last"));
        Assertions.assertTrue(s.contains("user123"));
    }

    @Test
    public void testTraineeGettersAndToString() {
        Date dob = new Date(1000L);
        UUID id = UUID.randomUUID();
        Trainee t = new Trainee("TFirst","TLast","tuser","tpw",false,dob,"addr",id);
        Assertions.assertEquals(dob, t.getDateOfBirth());
        Assertions.assertEquals("addr", t.getAddress());
        Assertions.assertEquals(id, t.getUserId());
        String s = t.toString();
        Assertions.assertTrue(s.contains("TFirst"));
        Assertions.assertTrue(s.contains("addr"));
        Assertions.assertTrue(s.contains(id.toString()));
    }

    @Test
    public void testTrainerGettersAndToString() {
        UUID id = UUID.randomUUID();
        Trainer tr = new Trainer("TrFirst","TrLast","truser","trpw",true,id,"strength");
        Assertions.assertEquals(id, tr.getUserId());
        Assertions.assertEquals("strength", tr.getSpecialization());
        String s = tr.toString();
        Assertions.assertTrue(s.contains("TrFirst"));
        Assertions.assertTrue(s.contains("strength"));
        Assertions.assertTrue(s.contains(id.toString()));
    }

    @Test
    public void testTrainingGettersAndToString() {
        UUID trainingId = UUID.randomUUID();
        UUID traineeId = UUID.randomUUID();
        UUID trainerId = UUID.randomUUID();
        Date date = new Date(2000L);
        TrainingType type = new TrainingType("cardio");
        Training tr = new Training(trainingId, traineeId, trainerId, "Run", type, date, Duration.ofMinutes(30));
        Assertions.assertEquals("Run", tr.getTrainingName());
        Assertions.assertEquals(type, tr.getTrainingType());
        Assertions.assertEquals(date, tr.getTrainingDate());
        Assertions.assertEquals(Duration.ofMinutes(30), tr.getTrainingDuration());
        String s = tr.toString();
        Assertions.assertTrue(s.contains("Run"));
        Assertions.assertTrue(s.contains("cardio"));
        Assertions.assertTrue(s.contains(trainingId.toString()));
    }

    @Test
    public void testTrainingTypeGettersAndToString() {
        TrainingType tt = new TrainingType("yoga");
        Assertions.assertEquals("yoga", tt.getTrainingTypeName());
        String s = tt.toString();
        Assertions.assertTrue(s.contains("yoga"));
    }
}
