package com.example.trainer_workload_service.config;

import com.example.trainer_workload_service.domain.TrainerWorkload;
import com.mongodb.ConnectionString;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
@Profile("local")
public class LocalMongoDatabaseReset implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(LocalMongoDatabaseReset.class);
    private static final String LOCAL_DATABASE = "trainer-workload-service-local";
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "::1");

    private final MongoTemplate mongoTemplate;
    private final String mongoUri;

    public LocalMongoDatabaseReset(
            MongoTemplate mongoTemplate,
            @Value("${spring.data.mongodb.uri}") String mongoUri) {
        this.mongoTemplate = mongoTemplate;
        this.mongoUri = mongoUri;
    }

    @Override
    public void run(ApplicationArguments args) {
        String databaseName = mongoTemplate.getDb().getName();
        var hosts = new ConnectionString(mongoUri).getHosts();
        boolean localConnection = !hosts.isEmpty() && hosts.stream()
                .map(LocalMongoDatabaseReset::hostname)
                .map(host -> host.toLowerCase(Locale.ROOT))
                .allMatch(LOCAL_HOSTS::contains);

        if (!LOCAL_DATABASE.equals(databaseName) || !localConnection) {
            throw new IllegalStateException(
                    "The local profile may only reset the local MongoDB database "
                            + LOCAL_DATABASE + " on localhost.");
        }

        logger.warn("Dropping local development MongoDB database '{}'", databaseName);
        mongoTemplate.getDb().drop();
        mongoTemplate.indexOps(TrainerWorkload.class).ensureIndex(
                new CompoundIndexDefinition(new Document("firstName", 1).append("lastName", 1))
                        .named("trainer_name_idx"));
    }

    private static String hostname(String hostAndPort) {
        if (hostAndPort.startsWith("[")) {
            int closingBracket = hostAndPort.indexOf(']');
            return closingBracket < 0 ? hostAndPort : hostAndPort.substring(1, closingBracket);
        }
        int colon = hostAndPort.lastIndexOf(':');
        return colon < 0 ? hostAndPort : hostAndPort.substring(0, colon);
    }
}
