package gym.repository;

import gym.domain.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TraineeRepository extends JpaRepository<Trainee, UUID> {
    Optional<Trainee> findByUser_Username(String username);

    void deleteByUser_Username(String username);
}
