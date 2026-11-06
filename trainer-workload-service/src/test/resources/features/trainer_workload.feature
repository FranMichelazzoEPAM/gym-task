Feature: Trainer workload API
  The workload service lets authorized clients retrieve a trainer's monthly or complete workload.

  Scenario: Retrieve a trainer's recorded monthly workload
    Given trainer "trainer1" has 2.5 training hours in month 3 of 2025
    When the client requests monthly workload for trainer "trainer1" in 2025-3 with a valid JWT
    Then the response status is 200
    And the monthly summary identifies trainer "trainer1", year 2025, month 3, with 2.5 total hours

  Scenario: Retrieve a trainer's complete workload
    Given trainer "trainer1" has 2.5 training hours in month 3 of 2025
    When the client requests complete workload for trainer "trainer1" with a valid JWT
    Then the response status is 200
    And the complete workload identifies trainer "trainer1" with name "Taylor" "Trainer" and 2.5 hours in month 3 of 2025

  Scenario: A trainer has no workload recorded for the requested month
    Given trainer "trainer1" has 2.5 training hours in month 3 of 2025
    When the client requests monthly workload for trainer "trainer1" in 2025-4 with a valid JWT
    Then the response status is 200
    And the monthly summary identifies trainer "trainer1", year 2025, month 4, with 0.0 total hours

  Scenario: Request a monthly summary for an unknown trainer
    When the client requests monthly workload for trainer "missing" in 2025-3 with a valid JWT
    Then the response status is 404
    And the error identifies status 404 and trainer "missing"

  Scenario: Request the complete workload for an unknown trainer
    When the client requests complete workload for trainer "missing" with a valid JWT
    Then the response status is 404
    And the error identifies status 404 and trainer "missing"

  Scenario: Reject a monthly request without a JWT
    When the client requests monthly workload for trainer "trainer1" in 2025-3 without a JWT
    Then the response status is 401

  Scenario: Reject a monthly request with an invalid JWT
    When the client requests monthly workload for trainer "trainer1" in 2025-3 with an invalid JWT
    Then the response status is 401

  Scenario: Reject a monthly request with an expired JWT
    When the client requests monthly workload for trainer "trainer1" in 2025-3 with an expired JWT
    Then the response status is 401

  Scenario: Reject a complete-workload request without a JWT
    When the client requests complete workload for trainer "trainer1" without a JWT
    Then the response status is 401

  Scenario: Reject a complete-workload request with an invalid JWT
    When the client requests complete workload for trainer "trainer1" with an invalid JWT
    Then the response status is 401

  Scenario: Reject a complete-workload request with an expired JWT
    When the client requests complete workload for trainer "trainer1" with an expired JWT
    Then the response status is 401
