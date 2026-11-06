Feature: Gym CRM and trainer workload service integration
  CRM training events are consumed by the workload service and exposed through authenticated REST queries.

  @integration @jms @workload-query @create-training
  Scenario: Creating a training updates the trainer's monthly and annual workload
    Given a trainee named "Mina" is registered as "trainee"
    And a trainer named "Taylor" with specialization "Cardio" is registered as "trainer"
    And I authenticate as "trainee"
    When I create a future training named "Inter-service morning session" of 45 minutes for trainee "trainee" with trainer "trainer"
    Then the CRM should accept the request with status 200
    And the CRM monthly workload for trainer "trainer" should eventually be 0.75 hours
    And the CRM full workload for trainer "trainer" should eventually include the session

  @integration @jms @workload-query @delete-training
  Scenario: Deleting a future training removes its duration from workload
    Given a trainee named "Noah" is registered as "trainee"
    And a trainer named "Riley" with specialization "Strength" is registered as "trainer"
    And I authenticate as "trainee"
    When I create a future training named "Inter-service removable session" of 60 minutes for trainee "trainee" with trainer "trainer"
    Then the CRM should accept the request with status 200
    And the CRM monthly workload for trainer "trainer" should eventually be 1 hour
    When I delete the created training
    Then the CRM should accept the request with status 204
    And the CRM monthly workload for trainer "trainer" should eventually be 0 hours

  @integration @negative @validation
  Scenario: Invalid training data does not publish workload
    Given a trainee named "Avery" is registered as "trainee"
    And a trainer named "Jordan" with specialization "Yoga" is registered as "trainer"
    And I authenticate as "trainee"
    When I try to create a training named "Invalid inter-service session" of 0 minutes for trainee "trainee" with trainer "trainer"
    Then the CRM should reject the request with status 400
    And the workload service should not have a record for trainer "trainer"

  @integration @security
  Scenario: The workload service rejects unauthenticated or invalid service tokens
    Then the workload service should reject unauthenticated and invalid JWT requests with status 401
