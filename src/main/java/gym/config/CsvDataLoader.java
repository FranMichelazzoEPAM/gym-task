package gym.config;

import gym.domain.Trainee;
import gym.domain.Trainer;
import gym.domain.Training;
import gym.domain.TrainingType;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CsvDataLoader {

    public List<Trainee> loadTrainees(InputStream inputStream) {
        List<Trainee> trainees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = reader.lines().toList();

            // Skip CSV header
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) {
                    continue;
                }

                String[] fields = lines.get(i).split(",", -1);

                UUID userId = UUID.fromString(fields[0]);
                String firstName = fields[1];
                String lastName = fields[2];
                String username = fields[3];
                String password = fields[4];
                boolean isActive = Boolean.parseBoolean(fields[5]);
                Date dateOfBirth = parseDate(fields[6]);
                String address = fields[7];

                Trainee trainee = new Trainee(
                        firstName,
                        lastName,
                        username,
                        password,
                        isActive,
                        dateOfBirth,
                        address,
                        userId
                );

                trainees.add(trainee);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Trainee CSV: ", e);
        }

        return trainees;
    }

    public List<Trainer> loadTrainers(InputStream inputStream) {
        List<Trainer> trainers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = reader.lines().toList();

            // Skip CSV header
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) {
                    continue;
                }

                String[] fields = lines.get(i).split(",", -1);

                UUID userId = UUID.fromString(fields[0]);
                String firstName = fields[1];
                String lastName = fields[2];
                String username = fields[3];
                String password = fields[4];
                boolean isActive = Boolean.parseBoolean(fields[5]);
                String specialization = fields[6];

                Trainer trainer = new Trainer(
                        firstName,
                        lastName,
                        username,
                        password,
                        isActive,
                        userId,
                        specialization
                );

                trainers.add(trainer);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Trainers CSV: ", e);
        }

        return trainers;
    }

    public List<Training> loadTrainings(InputStream inputStream) {
        List<Training> trainings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = reader.lines().toList();

            // Skip CSV header
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) {
                    continue;
                }

                String[] fields = lines.get(i).split(",", -1);

                UUID trainingId = UUID.fromString(fields[0]);
                UUID traineeId = UUID.fromString(fields[1]);
                UUID trainerId = UUID.fromString(fields[2]);
                String trainingName = fields[3];

                TrainingType trainingType =
                        new TrainingType(fields[4]);

                Date trainingDate =
                        parseDateTime(fields[5]);

                Duration trainingDuration =
                        Duration.parse(fields[6]);

                Training training = new Training(
                        trainingId,
                        traineeId,
                        trainerId,
                        trainingName,
                        trainingType,
                        trainingDate,
                        trainingDuration
                );

                trainings.add(training);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Trainings CSV: ", e);
        }

        return trainings;
    }

    private Date parseDate(String value) {
        LocalDate localDate = LocalDate.parse(value);

        return Date.from(
                localDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    private Date parseDateTime(String value) {
        LocalDateTime localDateTime = LocalDateTime.parse(value);

        return Date.from(
                localDateTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }
}