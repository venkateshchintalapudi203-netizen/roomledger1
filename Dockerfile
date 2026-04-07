# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# JRE build stage - create minimal JRE
FROM eclipse-temurin:17 AS jre-build
RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.logging,java.xml,java.desktop,java.management,java.sql,java.naming \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /custom-jre

# Run stage
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=jre-build /custom-jre /opt/java/
COPY --from=build /app/target/*.jar app.jar
ENV PATH="/opt/java/bin:${PATH}"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]