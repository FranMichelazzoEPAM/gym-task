package gym.mapper;

import gym.domain.Trainer;
import gym.dto.response.*;

import java.util.List;

public class TrainerMapper {
    private TrainerMapper() {}

    public static CredentialsResponse toCredentialsResponse(Trainer trainer) {
        return new CredentialsResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword()
        );
    }

    public static TrainerSummaryResponse toSummary(Trainer trainer) {
        return new TrainerSummaryResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public static TrainerProfileResponse toProfileResponse(Trainer trainer) {
        List<TraineeSummaryResponse> traineeSummaries = trainer.getTrainees().stream()
                .map(TraineeMapper::toSummary)
                .toList();

        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().isActive(),
                traineeSummaries
        );
    }

    public static TrainerUpdateResponse toUpdateResponse(Trainer trainer) {
        List<TraineeSummaryResponse> traineeSummaries = trainer.getTrainees().stream()
                .map(TraineeMapper::toSummary)
                .toList();

        return new TrainerUpdateResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().isActive(),
                traineeSummaries);
    }
}
