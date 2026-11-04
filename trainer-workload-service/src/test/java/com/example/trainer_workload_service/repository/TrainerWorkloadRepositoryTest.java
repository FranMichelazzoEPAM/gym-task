package com.example.trainer_workload_service.repository;

import com.example.trainer_workload_service.domain.MonthSummary;
import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.example.trainer_workload_service.domain.YearSummary;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
class TrainerWorkloadRepositoryTest {

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String savedUsername;

    @AfterEach
    void cleanUp() {
        if (savedUsername != null) {
            repository.deleteById(savedUsername);
        }
    }

    @Test
    void savesAndRetrievesTrainerWithNestedYearAndMonthSummaries() {
        savedUsername = "repository-test-" + UUID.randomUUID();
        TrainerWorkload trainer = new TrainerWorkload(savedUsername, "Taylor", "Morgan", true);
        YearSummary year = new YearSummary(2026);
        year.setMonths(List.of(new MonthSummary(11, 12.5)));
        trainer.setYears(List.of(year));

        repository.save(trainer);

        TrainerWorkload retrieved = repository.findById(savedUsername).orElseThrow();
        TrainerWorkload foundByName = repository.findByFirstNameAndLastName("Taylor", "Morgan").stream()
                .filter(result -> result.getUsername().equals(savedUsername))
                .findFirst()
                .orElseThrow();
        Document storedDocument = mongoTemplate.getCollection("trainer_workloads")
                .find(new Document("_id", savedUsername))
                .first();
        List<Document> storedYears = storedDocument.getList("years", Document.class);
        Object storedDuration = storedYears.get(0).getList("months", Document.class)
                .get(0).get("trainingSummaryDuration");
        assertEquals(savedUsername, retrieved.getUsername());
        assertEquals(savedUsername, foundByName.getUsername());
        assertEquals("Taylor", retrieved.getFirstName());
        assertEquals("Morgan", retrieved.getLastName());
        assertTrue(retrieved.isActive());
        assertEquals(1, retrieved.getYears().size());
        assertEquals(2026, retrieved.getYears().get(0).getYear());
        assertEquals(1, retrieved.getYears().get(0).getMonths().size());
        assertEquals(11, retrieved.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(12.5, retrieved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
        assertInstanceOf(Boolean.class, storedDocument.get("active"));
        assertInstanceOf(Number.class, storedDuration);
    }

    @Test
    void createsCompoundIndexForTrainerFirstAndLastName() {
        List<IndexInfo> indexes = mongoTemplate.indexOps(TrainerWorkload.class).getIndexInfo();

        assertTrue(indexes.stream().anyMatch(index ->
                index.getName().equals("trainer_name_idx")
                        && index.getIndexFields().stream()
                        .map(field -> field.getKey())
                        .toList()
                        .equals(List.of("firstName", "lastName"))));
    }
}
