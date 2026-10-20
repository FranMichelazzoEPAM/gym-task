package com.example.trainer_workload_service.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TrainerWorkload {
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
