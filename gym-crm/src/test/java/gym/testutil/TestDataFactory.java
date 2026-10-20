package gym.testutil;

import gym.domain.*;

import java.util.ArrayList;
import java.util.Date;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static User user(String firstName, String lastName, String username, String password, boolean active) {
        User u = new User(firstName, lastName, username, password, active);
        return u;
    }

    public static Trainer trainer(String firstName, String lastName, String username, String password, boolean active) {
        User user = user(firstName, lastName, username, password, active);
        return new Trainer(user, null);
    }

    public static Trainee trainee(String firstName, String lastName, String username, String password, boolean active) {
        User u = user(firstName, lastName, username, password, active);
        return new Trainee(u, new Date(), "Some address");
    }

    public static TrainingType trainingType(String name) {
        return new TrainingType(name);
    }

    public static Training training(TrainingType type, Trainee trainee, Trainer trainer, Date date, int durationMinutes) {
        Training t = new Training(trainee, trainer, type.getTrainingTypeName() + "-session", type, date, durationMinutes);
        return t;
    }
}
