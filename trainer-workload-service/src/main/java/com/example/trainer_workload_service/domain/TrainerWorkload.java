package com.example.trainer_workload_service.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document("trainer_workloads")
@CompoundIndex(name = "trainer_name_idx", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {

    @Id
    private String username;

    private String firstName;

    private String lastName;

    private boolean active;

    private List<YearSummary> years =  new ArrayList<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
}
