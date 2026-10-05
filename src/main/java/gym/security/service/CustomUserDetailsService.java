package gym.security.service;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.security.userDetails.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public CustomUserDetailsService(TraineeRepository traineeRepository,
                                    TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public UserDetails loadUserByUsername (String username) {
        Optional<Trainee> trainee = traineeRepository.findByUser_Username(username);
        if (trainee.isPresent()) {
            return new CustomUserDetails(trainee.get().getUser(), "TRAINEE");
        }

        Optional<Trainer> trainer = trainerRepository.findByUser_Username(username);
        if (trainer.isPresent()) {
            return new CustomUserDetails(trainer.get().getUser(), "TRAINER");
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
