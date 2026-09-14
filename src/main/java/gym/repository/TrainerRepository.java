package gym.repository;

import gym.domain.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {

    @Query("SELECT t FROM Trainer t LEFT JOIN FETCH t.specialization WHERE t.user.username = :username")
    Optional<Trainer> findByUser_Username(@Param("username") String username);

    @Query("SELECT DISTINCT t FROM Trainer t LEFT JOIN FETCH t.specialization")
    List<Trainer> findAll(); // override base method — same fix for getAllTrainers()

    @Query("SELECT tr FROM Trainer tr WHERE tr NOT IN " +
            "(SELECT t FROM Trainee tn JOIN tn.trainers t WHERE tn.user.username = :username)")
    List<Trainer> findUnassignedTrainersForTrainee(@Param("username") String username);
}