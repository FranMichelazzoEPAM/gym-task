package gym.service;

import gym.domain.Trainee;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TraineeService {
    Trainee createTrainee(String firstName, String lastName, Date dateOfBirth, String address);
    Trainee updateTrainee(Trainee trainee);
    void deleteTrainee(UUID userId);
    Trainee getTrainee(UUID userId);
    List<Trainee> getAllTrainees();
}
