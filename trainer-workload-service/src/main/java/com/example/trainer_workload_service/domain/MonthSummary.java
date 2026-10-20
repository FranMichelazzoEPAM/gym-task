package com.example.trainer_workload_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {
    private int month;
    private double trainingSummaryDuration; // hours, my implementation support fractional values
                                            // (for example, if I train 30 minutes that would be 0.5 hours)
}
