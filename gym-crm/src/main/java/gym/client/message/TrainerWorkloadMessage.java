package gym.client.message;

import gym.client.dto.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadMessage implements Serializable {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean active;
    private LocalDate trainingDate;
    private Integer trainingDuration; // in minutes
    private ActionType actionType;
}
