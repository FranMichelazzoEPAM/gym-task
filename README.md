Hi Rafael, here are some explanations regarding the endpoints:

For the trianer workload on a specific month (this was implemented before):
http://localhost:8080/api/trainers/{username}/workload/{year}/{month}

or if you want to access the complete document (new):
http://localhost:8080/api/trainers/{username}/workload

both are endpoints in port 8080 (gym crm) that are redirected to port 8081 (workload service)

### ActiveMQ
Remember you have to run ActiveMQ manually.

## Cucumber microservices integration tests

The separate integration suite starts both Spring Boot applications with the `integration` profile and verifies the real CRM-to-workload ActiveMQ event flow and authenticated workload REST queries. It uses the locally running ActiveMQ broker at `localhost:61616` and MongoDB at `localhost:27017`; start both before running the suite. ActiveMQ defaults to the `admin` / `admin` credentials, which can be overridden with the `ACTIVEMQ_USER` and `ACTIVEMQ_PASSWORD` environment variables.

Run the complete integration suite from the repository root:

```shell
mvn -Pmicroservice-integration verify
```

The Maven profile builds both apps, starts them on ports `18080` and `18081`, runs the Cucumber integration scenarios, and stops the application processes it started. It uses a temporary H2 database and a run-specific MongoDB database/ActiveMQ queue; the run-specific Mongo database is retained in local MongoDB. Integration features are in `gym-crm/src/test/resources/integration-features`. The existing per-service component Cucumber suites remain independently runnable.

The profile is opt-in and excluded from the default Maven test suites. Select scenarios using their Cucumber tags; for example, run only JMS workload flows with `mvn -Pmicroservice-integration -Dcucumber.filter.tags="@jms" verify`, only security scenarios with `mvn -Pmicroservice-integration -Dcucumber.filter.tags="@security" verify`, or all scenarios tagged `@integration` with `mvn -Pmicroservice-integration -Dcucumber.filter.tags="@integration" verify`. Scenario tags are defined in the feature file.

## Docker sub-task 1 (Under docker-subtask-1 tag)

The `gym-crm` and `trainer-workload-service` Dockerfiles build Java 21 images and disable Eureka discovery and application messaging in the containers.

Build both images from the repository root:

```powershell
docker build -t gym-crm:local .\gym-crm
docker build -t trainer-workload-service:local .\trainer-workload-service
```

If you want to check the images, run:

```powershell
docker image ls
```

Run each command in a separate terminal:

```powershell
docker run --rm --name gym-crm -p 8080:8080 gym-crm:local
docker run --rm --name trainer-workload-service -p 8081:8081 trainer-workload-service:local
```

Check that each HTTP server responds:

```powershell
curl.exe -f http://localhost:8080/v3/api-docs
curl.exe -f http://localhost:8081/v3/api-docs
```

Doing that, both services start correctly. However, it does not provide end-to-end workload behavior.
CRM workload notifications are disabled without ActiveMQ.
The CRM workload-query endpoints require the workload service to be reachable through Eureka or a
configured direct Feign URL. The workload service's storage-backed endpoints require MongoDB.

## Docker Compose end-to-end stack

The root `docker-compose.yml` runs CRM, the trainer workload service, MongoDB, and ActiveMQ on a
user-defined Docker network. It keeps Eureka disabled and uses Docker DNS for the Feign, JMS, and
MongoDB connections.

To start the stack from the repo root run:

```powershell
docker compose up --build -d
docker compose ps
```

MongoDB and ActiveMQ must become healthy before the workload service starts, and the workload
service must be healthy before CRM starts.

To stop the stack, just run `docker compose down`.
