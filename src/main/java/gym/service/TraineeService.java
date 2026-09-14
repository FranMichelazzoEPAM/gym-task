package gym.service;

import gym.domain.Trainee;

import java.util.Date;
import java.util.List;

public interface TraineeService {
    Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTrainee(Trainee trainee);
    void deleteTraineeByUsername(String username);
    Trainee getTraineeByUsername(String username);
    List<Trainee> getAllTrainees();

    boolean authenticate(String username, String password);
    void changePassword(String username, String oldPassword, String newPassword);
    void toggleActiveStatus(String username);
    Trainee updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames);
}
