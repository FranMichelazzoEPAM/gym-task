package gym.integrationbdd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public final class MicroservicesMavenRunner {

    private static final String WORKLOAD_JAR = "trainer-workload-service/target/"
            + "trainer-workload-service-0.0.1-SNAPSHOT.jar";
    private static final String CRM_JAR = "gym-crm/target/gym-task-1.0-SNAPSHOT.jar";
    private static final String WORKLOAD_SECRET =
            "gym-workload-integration-secret-key-at-least-32";
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(90);

    private MicroservicesMavenRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3 || (!args[0].equals("start") && !args[0].equals("stop"))) {
            throw new IllegalArgumentException(
                    "Usage: MicroservicesMavenRunner <start|stop> <repository-root> <state-file>");
        }

        Path repositoryRoot = Path.of(args[1]).toAbsolutePath().normalize();
        Path stateFile = Path.of(args[2]).toAbsolutePath().normalize();
        if (args[0].equals("start")) {
            start(repositoryRoot, stateFile);
        } else {
            stop(stateFile);
        }
    }

    private static void start(Path repositoryRoot, Path stateFile) throws Exception {
        requirePort(61616, "ActiveMQ");
        requirePort(27017, "MongoDB");
        requirePortAvailable(18080);
        requirePortAvailable(18081);

        Path workloadJar = repositoryRoot.resolve(WORKLOAD_JAR);
        Path crmJar = repositoryRoot.resolve(CRM_JAR);
        requireFile(workloadJar);
        requireFile(crmJar);

        String runId = UUID.randomUUID().toString().replace("-", "");
        String mongoDatabase = "gymit_" + runId;
        String queueName = "trainer.workload.integration." + runId;
        String deadLetterQueue = "trainer.workload.invalid.integration." + runId;
        Path runDirectory = repositoryRoot.resolve("target/microservice-integration").resolve(runId);
        Files.createDirectories(runDirectory);

        String workloadSecret = System.getenv().getOrDefault("GYM_IT_WORKLOAD_JWT_SECRET", WORKLOAD_SECRET);
        if (workloadSecret.length() < 32) {
            throw new IllegalArgumentException("GYM_IT_WORKLOAD_JWT_SECRET must contain at least 32 characters.");
        }

        Properties state = new Properties();
        state.setProperty("run.id", runId);
        state.setProperty("crm.base.url", "http://localhost:18080");
        state.setProperty("workload.base.url", "http://localhost:18081");
        state.setProperty("workload.jwt.secret", workloadSecret);
        state.setProperty("crm.database.path", runDirectory.resolve("gymcrm").toString());
        state.setProperty("state.file", stateFile.toString());
        state.setProperty("mongo.database", mongoDatabase);
        state.setProperty("queue.name", queueName);
        state.setProperty("dead-letter.queue", deadLetterQueue);
        Files.createDirectories(stateFile.getParent());

        List<Process> started = new ArrayList<>();
        try {
            Process workload = launch(
                    repositoryRoot, runDirectory.resolve("workload-service.log"), workloadJar,
                    Map.of(
                            "WORKLOAD_PORT", "18081",
                            "MONGODB_URI", mongoUri(mongoDatabase),
                            "WORKLOAD_JWT_SECRET", workloadSecret,
                            "WORKLOAD_QUEUE", queueName,
                            "WORKLOAD_DLQ", deadLetterQueue));
            started.add(workload);
            state.setProperty("workload.pid", Long.toString(workload.pid()));
            storeState(stateFile, state);
            awaitHttpStatus("http://localhost:18081/api/trainers/integration-probe/workload", 401, workload);

            Process crm = launch(
                    repositoryRoot, runDirectory.resolve("gym-crm.log"), crmJar,
                    Map.of(
                            "GYM_CRM_PORT", "18080",
                            "GYM_INTEGRATION_DB_PATH", state.getProperty("crm.database.path"),
                            "GYM_CRM_BASE_URL", state.getProperty("crm.base.url"),
                            "WORKLOAD_BASE_URL", state.getProperty("workload.base.url"),
                            "WORKLOAD_SERVICE_URL", state.getProperty("workload.base.url"),
                            "WORKLOAD_JWT_SECRET", workloadSecret,
                            "WORKLOAD_QUEUE", queueName,
                            "WORKLOAD_DLQ", deadLetterQueue));
            started.add(crm);
            state.setProperty("crm.pid", Long.toString(crm.pid()));
            storeState(stateFile, state);
            awaitHttpStatus("http://localhost:18080/actuator/health", 200, crm);
            System.out.println("Started integration services; run=" + runId + " MongoDB=" + mongoDatabase);
        } catch (Exception exception) {
            stopProcesses(started.stream().map(Process::pid).toList());
            throw exception;
        }
    }

    private static Process launch(
            Path workingDirectory, Path logFile, Path jar, java.util.Map<String, String> environment)
            throws IOException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        ProcessBuilder builder = new ProcessBuilder(
                java, "-jar", jar.toString(), "--spring.profiles.active=integration");
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(logFile.toFile());
        builder.environment().putIfAbsent("ACTIVEMQ_URL", "tcp://localhost:61616");
        builder.environment().putIfAbsent("ACTIVEMQ_USER", "admin");
        builder.environment().putIfAbsent("ACTIVEMQ_PASSWORD", "admin");
        builder.environment().putAll(environment);
        return builder.start();
    }

    private static void awaitHttpStatus(String url, int expectedStatus, Process process) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                throw new IllegalStateException("Application exited during startup; see its integration log.");
            }
            try {
                int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                if (status == expectedStatus) {
                    return;
                }
            } catch (IOException ignored) {
                Thread.sleep(500);
            }
        }
        throw new IllegalStateException("Application did not return HTTP " + expectedStatus + " from " + url);
    }

    private static void stop(Path stateFile) throws IOException {
        if (!Files.exists(stateFile)) {
            System.out.println("No integration service state file; nothing to stop.");
            return;
        }
        Properties state = new Properties();
        try (var input = Files.newInputStream(stateFile)) {
            state.load(input);
        }
        List<Long> processIds = new ArrayList<>();
        for (String key : List.of("crm.pid", "workload.pid")) {
            String value = state.getProperty(key);
            if (value != null) {
                processIds.add(Long.parseLong(value));
            }
        }
        stopProcesses(processIds);
        System.out.println("Stopped integration service processes.");
    }

    private static void stopProcesses(List<Long> processIds) {
        List<ProcessHandle> processes = processIds.stream()
                .map(ProcessHandle::of)
                .flatMap(java.util.Optional::stream)
                .filter(ProcessHandle::isAlive)
                .toList();
        processes.forEach(ProcessHandle::destroy);
        long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        while (System.nanoTime() < deadline && processes.stream().anyMatch(ProcessHandle::isAlive)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        processes.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
    }

    private static void requirePort(int port, String service) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1500);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    service + " is unavailable at localhost:" + port + "; start it before Maven integration tests.",
                    exception);
        }
    }

    private static void requirePortAvailable(int port) throws IOException {
        try (var server = new java.net.ServerSocket()) {
            server.setReuseAddress(false);
            server.bind(new InetSocketAddress("localhost", port));
        } catch (IOException exception) {
            throw new IllegalStateException("Integration test port " + port + " is already in use.", exception);
        }
    }

    private static void requireFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Required service JAR does not exist: " + path);
        }
    }

    private static String mongoUri(String database) {
        String configured = System.getenv().getOrDefault(
                "MONGODB_URI", "mongodb://localhost:27017/trainer-workload-service-integration");
        int authorityEnd = configured.indexOf('/', configured.indexOf("://") + 3);
        String host = authorityEnd < 0 ? configured : configured.substring(0, authorityEnd);
        String query = configured.indexOf('?') < 0 ? "" : configured.substring(configured.indexOf('?'));
        return host + "/" + database + query;
    }

    private static void storeState(Path stateFile, Properties state) throws IOException {
        try (var output = Files.newOutputStream(stateFile)) {
            state.store(output, "Cucumber microservice integration run");
        }
    }
}
