Feature: Trainee management
  Trainee profiles can be managed through the secured REST API.

  Scenario: A trainee can be registered and retrieved
    Given a trainee named "Jordan" exists as "trainee"
    And I am logged in as "trainee"
    When I send a GET request to "/api/trainees/${trainee.username}"
    Then the response status should be 200
    And the response JSON field "$.firstName" should equal "Jordan"

  Scenario: Invalid trainee registration is rejected
    When I send a POST request to "/api/trainees" with JSON:
      """
      {
        "firstName": "",
        "lastName": "ComponentTest"
      }
      """
    Then the response status should be 400
    And the response body should include "Validation failed"

  Scenario: A missing trainee profile returns not found
    Given a trainee named "Robin" exists as "trainee"
    And I am logged in as "trainee"
    When I send a GET request to "/api/trainees/unknown.trainee"
    Then the response status should be 404

  Scenario: A trainee can update their profile
    Given a trainee named "Sam" exists as "trainee"
    And I am logged in as "trainee"
    When I send a PUT request to "/api/trainees/${trainee.username}" with JSON:
      """
      {
        "firstName": "Samuel",
        "lastName": "ComponentTest",
        "dateOfBirth": "1990-01-01",
        "address": "Updated address",
        "active": true
      }
      """
    Then the response status should be 200
    When I send a GET request to "/api/trainees/${trainee.username}"
    Then the response status should be 200
    And the response JSON field "$.firstName" should equal "Samuel"

  Scenario: A trainee can be deactivated
    Given a trainee named "Cameron" exists as "trainee"
    And I am logged in as "trainee"
    When I send a PATCH request to "/api/trainees/status" with JSON:
      """
      {
        "username": "${trainee.username}",
        "active": false
      }
      """
    Then the response status should be 200
    When I send a GET request to "/api/trainees/${trainee.username}"
    Then the response status should be 200
    And the response JSON field "$.active" should equal "false"

  Scenario: Trainers can be assigned to a trainee
    Given training type "Cardio" exists
    And a trainee named "Harper" exists as "trainee"
    And a trainer named "Logan" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a PUT request to "/api/trainees/trainers-list/${trainee.username}" with JSON:
      """
      {
        "trainersUsernames": ["${trainer.username}"]
      }
      """
    Then the response status should be 200
    And the response body should include "Logan"

  Scenario: Trainee training history can be filtered
    Given training type "Cardio" exists
    And a trainee named "Avery" exists as "trainee"
    And a trainer named "Blake" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Filtered session",
        "date": "2099-12-31",
        "duration": 40
      }
      """
    And I send a GET request to "/api/trainees/${trainee.username}/trainings?periodFrom=2099-12-31&periodTo=2099-12-31&trainerName=Blake&trainingType=Cardio"
    Then the response status should be 200
    And the response JSON should contain 1 items
    And the response JSON field "$[0].trainingName" should equal "Filtered session"

  Scenario: Deleting a trainee removes their trainings
    Given training type "Cardio" exists
    And a trainee named "Devon" exists as "trainee"
    And a trainer named "Shawn" with specialization "Cardio" exists as "trainer"
    And I am logged in as "trainee"
    When I send a POST request to "/api/trainings" with JSON:
      """
      {
        "traineeUsername": "${trainee.username}",
        "trainerUsername": "${trainer.username}",
        "name": "Cascade session",
        "date": "2099-12-31",
        "duration": 40
      }
      """
    And I send a DELETE request to "/api/trainees/${trainee.username}"
    Then the response status should be 204
    And the database should contain 0 trainings
