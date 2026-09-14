package gym.repository;

import gym.domain.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
    Optional<Trainer> findByUser_Username(String username);

    // Requirement #17
    @Query("SELECT tr FROM Trainer tr WHERE tr NOT IN " +
            "(SELECT t FROM Trainee tn JOIN tn.trainers t WHERE tn.user.username = :username)")
    List<Trainer> findUnassignedTrainersForTrainee(@Param("username") String username);
}
