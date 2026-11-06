Feature: Authentication and authorization
  Component scenarios for login, JWT access, password changes, and account lockout.

  Scenario: A registered trainee can log in and receive a JWT
    Given a trainee named "Mina" exists as "trainee"
    When I log in as "trainee" with a valid password
    Then the response status should be 200
    And the response JSON field "$.token" should not be empty

  Scenario: Invalid credentials are rejected
    Given a trainee named "Noah" exists as "trainee"
    When I log in as "trainee" with an invalid password
    Then the response status should be 401

  Scenario: Repeated failed logins lock the account
    Given a trainee named "Ivy" exists as "trainee"
    When I submit three invalid login attempts and then a valid one as "trainee"
    Then the response status should be 423

  Scenario: A JWT is required to read a trainee profile
    Given a trainee named "Ari" exists as "trainee"
    When I send an unauthenticated GET request to "/api/trainees/${trainee.username}"
    Then the response status should be 403
    When I am logged in as "trainee"
    And I send a GET request to "/api/trainees/${trainee.username}"
    Then the response status should be 200
    And the response JSON field "$.firstName" should equal "Ari"

  Scenario: A trainee can change their password
    Given a trainee named "Kai" exists as "trainee"
    And I am logged in as "trainee"
    When I send a PUT request to "/api/auth/change-password" with JSON:
      """
      {
        "username": "${trainee.username}",
        "oldPassword": "${trainee.password}",
        "newPassword": "NewSecret123!"
      }
      """
    Then the response status should be 200
    When I log in as "trainee" with password "NewSecret123!"
    Then the response status should be 200

  Scenario: Logging out revokes the current JWT
    Given a trainee named "Remy" exists as "trainee"
    And I am logged in as "trainee"
    When I send a POST request to "/api/auth/logout"
    Then the response status should be 200
    When I send a GET request to "/api/trainees/${trainee.username}"
    Then the response status should be 401
