Feature: Trainer workload query
  Workload responses come from the separate workload service boundary.

  Scenario: A trainer workload summary is returned
    Given a trainer named "Finley" with specialization "Yoga" exists as "trainer"
    And I am logged in as "trainer"
    And the workload service returns a monthly total of 75.5 for trainer "trainer" in 10 month 2026
    When I send a GET request to "/api/trainers/${trainer.username}/workload/2026/10"
    Then the response status should be 200
    And the response JSON field "$.totalDuration" should equal "75.5"

  Scenario: A trainer workload summary is returned without filters
    Given a trainer named "Dakota" with specialization "Yoga" exists as "trainer"
    And I am logged in as "trainer"
    And the workload service returns a summary for trainer "trainer"
    When I send a GET request to "/api/trainers/${trainer.username}/workload"
    Then the response status should be 200
    And the response JSON field "$.username" should equal "${trainer.username}"

  Scenario: A workload service failure becomes a server error
    Given a trainer named "Skyler" with specialization "Yoga" exists as "trainer"
    And I am logged in as "trainer"
    And the workload service is unavailable for trainer "trainer"
    When I send a GET request to "/api/trainers/${trainer.username}/workload"
    Then the response status should be 500
    And the response body should include "Internal server error"
