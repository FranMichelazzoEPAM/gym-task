package gym.mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TrainingMapper {
    private TrainingMapper() {
        // Utility class, no instances are created
    }

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
