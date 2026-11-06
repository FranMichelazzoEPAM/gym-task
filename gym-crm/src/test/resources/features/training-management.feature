Feature: Training management
  Training creation and deletion follow the API validation and future-date rules.

  Scenario: A trainee can create a training with a trainer
    Given training type "Cardio" exists
    And a trainee named "Drew" exists as "trainee"
    And a trainer named "Alex" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Morning session",
        "date": "2099-12-31",
        "duration": 45
      }
      """
    Then the response status should be 200
    And the database should contain 1 trainings

  Scenario: A training with a non-positive duration is rejected
    Given training type "Cardio" exists
    And a trainee named "Emery" exists as "trainee"
    And a trainer named "Lee" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Invalid session",
        "date": "2099-12-31",
        "duration": 0
      }
      """
    Then the response status should be 400
    And the database should contain 0 trainings

  Scenario: A training cannot be created for an unknown trainer
    Given a trainee named "Reese" exists as "trainee"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "unknown.trainer",
        "name": "Missing trainer",
        "date": "2099-12-31",
        "duration": 45
      }
      """
    Then the response status should be 404
    And the database should contain 0 trainings

  Scenario: A future training can be deleted
    Given training type "Cardio" exists
    And a trainee named "Jamie" exists as "trainee"
    And a trainer named "Dana" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Future session",
        "date": "2099-12-31",
        "duration": 30
      }
      """
    And I delete the created training
    Then the response status should be 204
    And the database should contain 0 trainings

  Scenario: A past training cannot be deleted
    Given training type "Cardio" exists
    And a trainee named "Parker" exists as "trainee"
    And a trainer named "Quinn" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Past session",
        "date": "2000-01-01",
        "duration": 30
      }
      """
    And I delete the created training
    Then the response status should be 409
    And the database should contain 1 trainings
