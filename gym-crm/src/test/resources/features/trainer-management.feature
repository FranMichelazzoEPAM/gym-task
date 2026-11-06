Feature: Trainer management
  Trainer profiles, specialization, availability, and status are part of the component API.

  Scenario: A trainer can be registered and retrieved
    Given a trainer named "Taylor" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainer"
    When I send a GET request to "/api/trainers/${trainer.username}"
    Then the response status should be 200
    And the response JSON field "$.specialization" should equal "Cardio"

  Scenario: A trainer cannot be registered with an unknown specialization
    When I send a POST request to "/api/trainers" with JSON:
      """
      {
        "firstName": "Casey",
        "lastName": "ComponentTest",
        "specialization": "Unlisted"
      }
      """
    Then the response status should be 404

  Scenario: A trainer can be deactivated
    Given a trainer named "Morgan" with specialization "Strength" exists as "trainer"
    And I am logged in as "trainer"
    When I send a PATCH request to "/api/trainers/status" with JSON:
      """
      {
        "username": "${trainer.username}",
        "active": false
      }
      """
    Then the response status should be 200
    When I send a GET request to "/api/trainers/${trainer.username}"
    Then the response status should be 200
    And the response JSON field "$.active" should equal "false"

  Scenario: A trainer can update their profile
    Given a trainer named "Bailey" with specialization "Strength" exists as "trainer"
    And training type "Yoga" exists
    And I am logged in as "trainer"
    When I send a PUT request to "/api/trainers/${trainer.username}" with JSON:
      """
      {
        "firstName": "Bailey",
        "lastName": "Updated",
        "specialization": "Yoga",
        "active": true
      }
      """
    Then the response status should be 200
    And the response JSON field "$.specialization" should equal "Yoga"

  Scenario: Active unassigned trainers can be listed for a trainee
    Given training type "Cardio" exists
    And a trainee named "Shiloh" exists as "trainee"
    And a trainer named "Kendall" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a GET request to "/api/trainers/not-assigned-on-trainee/${trainee.username}"
    Then the response status should be 200
    And the response body should include "Kendall"

  Scenario: Trainer training history can be filtered
    Given training type "Yoga" exists
    And a trainee named "Micah" exists as "trainee"
    And a trainer named "Noel" with specialization "Yoga" exists as "trainer"
    And I am logged in as "trainer"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Trainer history session",
        "date": "2099-12-31",
        "duration": 50
      }
      """
    And I send a GET request to "/api/trainers/${trainer.username}/trainings?periodFrom=2099-12-31&periodTo=2099-12-31&traineeName=Micah"
    Then the response status should be 200
    And the response JSON should contain 1 items
    And the response JSON field "$[0].trainingName" should equal "Trainer history session"

  Scenario: Available training types can be listed
    Given training type "Cardio" exists
    And a trainer named "Riley" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainer"
    When I send a GET request to "/api/training-types"
    Then the response status should be 200
    And the response body should include "Cardio"
