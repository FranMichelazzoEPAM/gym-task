package com.example.trainer_workload_service.repository;

import com.example.trainer_workload_service.domain.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {
    List<TrainerWorkload> findByFirstNameAndLastName(String firstName, String lastName);
}
