package gym.mapper;

import gym.domain.Trainee;
import gym.dto.response.CredentialsResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TraineeMapper {
    private TraineeMapper() {
        // Utility class, no instances
    }
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static CredentialsResponse toCredentialsResponse(Trainee trainee) {
        return new CredentialsResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword()
        );
    }
}
