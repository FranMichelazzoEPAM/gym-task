package com.example.trainer_workload_service.service;

import com.example.trainer_workload_service.domain.MonthSummary;
import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.example.trainer_workload_service.domain.YearSummary;
import com.example.trainer_workload_service.dto.MonthlyWorkloadResponse;
import com.example.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.example.trainer_workload_service.exception.TrainerNotFoundException;
import com.example.trainer_workload_service.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;

    public TrainerWorkloadServiceImpl(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    @Override
    public void processWorkload(TrainerWorkloadRequest request) {
        TrainerWorkload workload = repository.findById(request.getUsername())
                .orElseGet(() -> new TrainerWorkload(
                        request.getUsername(),
                        request.getFirstName(),
                        request.getLastName(),
                        request.getIsActive()));

        workload.setFirstName(request.getFirstName());
        workload.setLastName(request.getLastName());
        workload.setActive(request.getIsActive());

        LocalDate date = request.getTrainingDate();
        YearSummary yearSummary = findOrCreateYear(workload, date.getYear());
        MonthSummary monthSummary = findOrCreateMonth(yearSummary, date.getMonthValue());

        double delta = switch (request.getActionType()) {
            case ADD -> request.getTrainingDuration();
            case DELETE -> -request.getTrainingDuration();
        };

        double updated = monthSummary.getTrainingSummaryDuration() + delta;
        monthSummary.setTrainingSummaryDuration(Math.max(updated, 0.0));

        repository.save(workload);
    }

    @Override
    public MonthlyWorkloadResponse getMonthlySummary(String username, int year, int month) {
        TrainerWorkload workload = repository.findById(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));

        double total = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .flatMap(y -> y.getMonths().stream()
                        .filter(m -> m.getMonth() == month)
                        .findFirst())
                .map(MonthSummary::getTrainingSummaryDuration)
                .orElse(0.0);

        return new MonthlyWorkloadResponse(username, year, month, total);
    }

    @Override
    public TrainerWorkload getTrainerWorkload(String username) {
        return repository.findById(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));
    }

    private YearSummary findOrCreateYear(TrainerWorkload workload, int year) {
        Optional<YearSummary> existing = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst();
        if (existing.isPresent()) return existing.get();

        YearSummary created = new YearSummary(year);
        workload.getYears().add(created);
        return created;
    }

    private MonthSummary findOrCreateMonth(YearSummary yearSummary, int month) {
        Optional<MonthSummary> existing = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst();
        if (existing.isPresent()) return existing.get();

        MonthSummary created = new MonthSummary(month, 0.0);
        yearSummary.getMonths().add(created);
        return created;
    }
}
