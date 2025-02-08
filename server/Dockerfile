# Build stage
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -pl server -am

# Runtime stage
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
COPY server/docker-entrypoint.sh .

RUN chmod +x docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]