FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src

RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<toolchains>' \
    '  <toolchain>' \
    '    <type>jdk</type>' \
    '    <provides><version>21</version></provides>' \
    '    <configuration><jdkHome>/opt/java/openjdk</jdkHome></configuration>' \
    '  </toolchain>' \
    '</toolchains>' > /root/.m2/toolchains.xml \
    && mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/trainer-workload-service-0.0.1-SNAPSHOT.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=docker \
    EUREKA_CLIENT_ENABLED=false \
    SPRING_CLOUD_DISCOVERY_ENABLED=false \
    SPRING_JMS_LISTENER_AUTO_STARTUP=false \
    APP_MESSAGING_ENABLED=false

EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
